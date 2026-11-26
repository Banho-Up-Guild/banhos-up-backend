package com.github.banhosup.service.appoiintment;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.availability.AvailabilityCalendarDayResponse;
import com.github.banhosup.dto.availability.AvailableEmployeeResponse;
import com.github.banhosup.dto.availability.StoreAvailabilityDayRequest;
import com.github.banhosup.dto.availability.StoreAvailabilityDayResponse;
import com.github.banhosup.dto.availability.StoreAvailabilityRequest;
import com.github.banhosup.model.entities.employee.EmployeeEntity;
import com.github.banhosup.model.entities.store.StoreBusinessHoursEntity;
import com.github.banhosup.model.entities.store.StoreEntity;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.employee.EmployeeRepository;
import com.github.banhosup.repository.store.StoreBusinessHoursRepository;
import com.github.banhosup.repository.store.StoreRepository;
import com.github.banhosup.repository.appointment.AppointmentsRepository;
import com.github.banhosup.repository.appointment.AppointmentsPetOfferingRepository;
import com.github.banhosup.model.entities.appointments.AppointmentsPetOffering;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private static final int INTERVAL_MINUTES = 30;

    private final StoreBusinessHoursRepository hoursRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final AppointmentsPetOfferingRepository appointmentsPetOfferingRepository;

    @Autowired
    public AvailabilityService(
            StoreBusinessHoursRepository hoursRepository,
            StoreRepository storeRepository,
            EmployeeRepository employeeRepository,
            AppointmentsRepository appointmentsRepository,
            AppointmentsPetOfferingRepository appointmentsPetOfferingRepository
    ) {
        this.hoursRepository = hoursRepository;
        this.storeRepository = storeRepository;
        this.employeeRepository = employeeRepository;
        this.appointmentsRepository = appointmentsRepository;
        this.appointmentsPetOfferingRepository = appointmentsPetOfferingRepository;
    }

    // Construtor de compatibilidade para testes unitários antigos.
    // Sem os itens associados, compromissos antigos são tratados como slots de 30 minutos.
    public AvailabilityService(
            StoreBusinessHoursRepository hoursRepository,
            StoreRepository storeRepository,
            EmployeeRepository employeeRepository,
            AppointmentsRepository appointmentsRepository
    ) {
        this(
                hoursRepository,
                storeRepository,
                employeeRepository,
                appointmentsRepository,
                null
        );
    }

    // ADMIN: consulta os horários de qualquer unidade.
    @Transactional(readOnly = true)
    public List<StoreAvailabilityDayResponse> listarPorUnidade(
            Long storeId
    ) {

        buscarUnidade(storeId);

        return montarSemana(storeId);
    }

    // ADMIN: altera a programação semanal de uma unidade.
    @Transactional
    public List<StoreAvailabilityDayResponse> salvarPorUnidade(
            Long storeId,
            StoreAvailabilityRequest request
    ) {

        StoreEntity store = buscarUnidade(storeId);

        return salvarSemana(store, request);
    }

    // FUNCIONÁRIO: consulta os horários da própria unidade.
    @Transactional(readOnly = true)
    public List<StoreAvailabilityDayResponse> listarMinhaUnidade(
            Authentication authentication
    ) {

        EmployeeEntity employee = buscarFuncionario(authentication);

        return montarSemana(employee.getStore().getId());
    }

    // FUNCIONÁRIO: altera os horários da própria unidade.
    @Transactional
    public List<StoreAvailabilityDayResponse> salvarMinhaUnidade(
            Authentication authentication,
            StoreAvailabilityRequest request
    ) {

        EmployeeEntity employee = buscarFuncionario(authentication);

        return salvarSemana(employee.getStore(), request);
    }

    private List<StoreAvailabilityDayResponse> salvarSemana(
            StoreEntity store,
            StoreAvailabilityRequest request
    ) {

        validarSemana(request);

        Map<Integer, StoreBusinessHoursEntity> existentes =
                hoursRepository
                        .findAllByStore_IdOrderByDayOfWeekAsc(store.getId())
                        .stream()
                        .collect(Collectors.toMap(
                                StoreBusinessHoursEntity::getDayOfWeek,
                                Function.identity()
                        ));

        List<StoreBusinessHoursEntity> novos = new ArrayList<>();

        for (StoreAvailabilityDayRequest day : request.days()) {

            validarHorario(day);

            StoreBusinessHoursEntity entity =
                    existentes.get(day.dayOfWeek());

            if (entity == null) {
                entity = new StoreBusinessHoursEntity();
            }

            entity.setStore(store);
            entity.setDayOfWeek(day.dayOfWeek());
            entity.setClosed(day.closed());

            if (day.closed()) {

                entity.setOpeningTime(null);
                entity.setClosingTime(null);
                entity.setBreakStart(null);
                entity.setBreakEnd(null);

            } else {

                entity.setOpeningTime(day.openingTime());
                entity.setClosingTime(day.closingTime());
                entity.setBreakStart(day.breakStart());
                entity.setBreakEnd(day.breakEnd());

            }

            novos.add(entity);
        }

        hoursRepository.saveAll(novos);

        return montarSemana(store.getId());
    }

    private void validarSemana(StoreAvailabilityRequest request) {

        if (request == null || request.days() == null ||
                request.days().size() != 7) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "É obrigatório informar os sete dias da semana."
            );
        }

        Set<Integer> dias = new HashSet<>();

        for (StoreAvailabilityDayRequest day : request.days()) {

            if (day.dayOfWeek() == null ||
                    day.dayOfWeek() < 1 ||
                    day.dayOfWeek() > 7) {

                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "O dia da semana deve estar entre 1 e 7."
                );
            }

            if (!dias.add(day.dayOfWeek())) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Não é permitido repetir dias da semana."
                );
            }

            if (day.closed() == null) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Informe se o estabelecimento estará fechado."
                );
            }
        }
    }

    private void validarHorario(StoreAvailabilityDayRequest day) {

        if (day.closed()) {
            return;
        }

        if (day.openingTime() == null ||
                day.closingTime() == null) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Informe os horários de abertura e fechamento."
            );
        }

        if (!day.openingTime().isBefore(day.closingTime())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A abertura deve ocorrer antes do fechamento."
            );
        }

        boolean temInicioIntervalo = day.breakStart() != null;
        boolean temFimIntervalo = day.breakEnd() != null;

        if (temInicioIntervalo != temFimIntervalo) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o início e o fim do intervalo."
            );
        }

        if (temInicioIntervalo) {

            if (!day.breakStart().isBefore(day.breakEnd()) ||
                    day.breakStart().isBefore(day.openingTime()) ||
                    day.breakEnd().isAfter(day.closingTime())) {

                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "O intervalo precisa estar dentro do horário de funcionamento."
                );
            }
        }
    }

    private List<StoreAvailabilityDayResponse> montarSemana(
            Long storeId
    ) {

        Map<Integer, StoreBusinessHoursEntity> horarios =
                hoursRepository
                        .findAllByStore_IdOrderByDayOfWeekAsc(storeId)
                        .stream()
                        .collect(Collectors.toMap(
                                StoreBusinessHoursEntity::getDayOfWeek,
                                Function.identity()
                        ));

        List<StoreAvailabilityDayResponse> resposta =
                new ArrayList<>();

        for (int dia = 1; dia <= 7; dia++) {

            StoreBusinessHoursEntity entity = horarios.get(dia);

            DayOfWeek dayOfWeek = DayOfWeek.of(dia);

            String nomeDia = dayOfWeek.getDisplayName(
                    TextStyle.FULL,
                    Locale.forLanguageTag("pt-BR")
            );

            if (entity == null) {

                resposta.add(new StoreAvailabilityDayResponse(
                        dia,
                        nomeDia,
                        true,
                        null,
                        null,
                        null,
                        null
                ));

            } else {

                resposta.add(new StoreAvailabilityDayResponse(
                        dia,
                        nomeDia,
                        entity.isClosed(),
                        entity.getOpeningTime(),
                        entity.getClosingTime(),
                        entity.getBreakStart(),
                        entity.getBreakEnd()
                ));
            }
        }

        return resposta;
    }

    // CALENDÁRIO PÚBLICO: separa horários livres dos totalmente ocupados.
    @Transactional(readOnly = true)
    public List<AvailabilityCalendarDayResponse> consultarCalendario(
            Long storeId,
            Long employeeId,
            YearMonth yearMonth,
            Integer durationMinutes
    ) {

        StoreEntity store = buscarUnidade(storeId);

        if (!store.isAtiva()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A unidade está inativa."
            );
        }

        if (yearMonth == null) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O mês de consulta é obrigatório."
            );
        }

        if (durationMinutes == null ||
                durationMinutes < 1 ||
                durationMinutes > 720) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A duração deve estar entre 1 e 720 minutos."
            );
        }

        List<EmployeeEntity> employees;

        if (employeeId != null && employeeId < 0) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O identificador do profissional não pode ser negativo."
            );
        }

        if (employeeId != null && employeeId > 0) {

            EmployeeEntity employee = employeeRepository
                    .findByIdAndStore_Id(employeeId, storeId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND,
                            "Profissional não encontrado nesta unidade."
                    ));

            if (!employee.getUser().isActive()) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "O profissional está inativo."
                );
            }

            if (!employee.isTeamActive()) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "O profissional não está mais vinculado à equipe desta unidade."
                );
            }

            employees = List.of(employee);

        } else {

            employees = employeeRepository
                    .findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(
                            storeId
                    );
        }

        if (employees.isEmpty()) {
            return List.of();
        }

        List<StoreBusinessHoursEntity> weeklyHours =
                hoursRepository.findAllByStore_IdOrderByDayOfWeekAsc(
                        storeId
                );

        Map<Integer, StoreBusinessHoursEntity> schedule =
                weeklyHours.stream()
                        .collect(Collectors.toMap(
                                StoreBusinessHoursEntity::getDayOfWeek,
                                Function.identity()
                        ));

        LocalDateTime monthStart =
                yearMonth.atDay(1).atStartOfDay();

        LocalDateTime monthEnd =
                yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<Long> employeeIds = employees.stream()
                .map(EmployeeEntity::getId)
                .toList();

        List<Appointments> appointments =
                appointmentsRepository
                        .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                                employeeIds,
                                monthStart,
                                monthEnd,
                                StatusAppointments.CANCELED
                        );

        Map<Long, List<BusyPeriod>> busyPeriodsByEmployee =
                montarPeriodosOcupados(appointments);

        LocalDateTime now = LocalDateTime.now();

        List<AvailabilityCalendarDayResponse> result =
                new ArrayList<>();

        for (LocalDate date = yearMonth.atDay(1);
             !date.isAfter(yearMonth.atEndOfMonth());
             date = date.plusDays(1)) {

            StoreBusinessHoursEntity daySchedule =
                    schedule.get(date.getDayOfWeek().getValue());

            if (daySchedule == null || daySchedule.isClosed()) {
                continue;
            }

            List<LocalTime> availableHours = new ArrayList<>();
            List<LocalTime> unavailableHours = new ArrayList<>();

            LocalTime slot = daySchedule.getOpeningTime();

            while (!slot.plusMinutes(durationMinutes)
                    .isAfter(daySchedule.getClosingTime())) {

                LocalTime slotEnd =
                        slot.plusMinutes(durationMinutes);

                boolean overlapsBreak =
                        daySchedule.getBreakStart() != null &&
                                slot.isBefore(daySchedule.getBreakEnd()) &&
                                slotEnd.isAfter(daySchedule.getBreakStart());

                LocalDateTime slotStartDateTime =
                        LocalDateTime.of(date, slot);

                boolean isPast =
                        !slotStartDateTime.isAfter(now);

                if (!overlapsBreak && !isPast) {

                    LocalDateTime slotEndDateTime =
                            LocalDateTime.of(date, slotEnd);

                    boolean availableForSomeone =
                            employees.stream().anyMatch(employee ->
                                    !possuiSobreposicao(
                                            busyPeriodsByEmployee,
                                            employee.getId(),
                                            slotStartDateTime,
                                            slotEndDateTime
                                    )
                            );

                    if (availableForSomeone) {
                        availableHours.add(slot);
                    } else {
                        unavailableHours.add(slot);
                    }
                }

                slot = slot.plusMinutes(INTERVAL_MINUTES);
            }

            if (!availableHours.isEmpty() || !unavailableHours.isEmpty()) {

                result.add(new AvailabilityCalendarDayResponse(
                        date,
                        availableHours,
                        unavailableHours
                ));
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public AvailableEmployeeResponse selecionarFuncionario(
            Long storeId,
            Long employeeId,
            LocalDateTime dateTime,
            Integer durationMinutes
    ) {

        StoreEntity store = buscarUnidade(storeId);

        if (!store.isAtiva()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A unidade está inativa."
            );
        }

        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A data e a hora devem ser futuras."
            );
        }

        validarDuracao(durationMinutes);

        if (employeeId != null && employeeId < 0) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O identificador do profissional não pode ser negativo."
            );
        }

        List<EmployeeEntity> employees;

        if (employeeId != null && employeeId > 0) {

            EmployeeEntity employee = employeeRepository
                    .findByIdAndStore_Id(employeeId, storeId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND,
                            "Profissional não encontrado nesta unidade."
                    ));

            if (!employee.getUser().isActive()) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "O profissional está inativo."
                );
            }

            if (!employee.isTeamActive()) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "O profissional não está mais vinculado à equipe desta unidade."
                );
            }

            employees = List.of(employee);

        } else {

            employees = employeeRepository
                    .findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(
                            storeId
                    );
        }

        if (employees.isEmpty()) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Não há profissionais ativos nesta unidade."
            );
        }

        StoreBusinessHoursEntity schedule = hoursRepository
                .findAllByStore_IdOrderByDayOfWeekAsc(storeId)
                .stream()
                .filter(day -> day.getDayOfWeek() == dateTime.getDayOfWeek().getValue())
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "A unidade não possui horário de funcionamento para esse dia."
                ));

        LocalTime slot = dateTime.toLocalTime();
        LocalTime slotEnd = slot.plusMinutes(durationMinutes);

        boolean overlapsBreak = schedule.getBreakStart() != null
                && slot.isBefore(schedule.getBreakEnd())
                && slotEnd.isAfter(schedule.getBreakStart());

        if (schedule.isClosed()
                || slot.isBefore(schedule.getOpeningTime())
                || slotEnd.isAfter(schedule.getClosingTime())
                || overlapsBreak) {

            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Não há profissional disponível nesse horário."
            );
        }

        List<Long> employeeIds = employees.stream()
                .map(EmployeeEntity::getId)
                .toList();

        LocalDateTime dayStart = dateTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dateTime.toLocalDate().atTime(LocalTime.MAX);

        List<Appointments> appointmentsForDay = appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
                        employeeIds,
                        dayStart,
                        dayEnd,
                        StatusAppointments.CANCELED
                );

        List<Appointments> futureAppointments = appointmentsRepository
                .findByEmployee_IdInAndDateTimeAppointmentAfterAndStatusNot(
                        employeeIds,
                        LocalDateTime.now(),
                        StatusAppointments.CANCELED
                );

        Map<Long, Long> appointmentCountByEmployee = new HashMap<>();

        employees.forEach(employee ->
                appointmentCountByEmployee.put(employee.getId(), 0L)
        );

        for (Appointments appointment : futureAppointments) {
            appointmentCountByEmployee.merge(
                    appointment.getEmployee().getId(),
                    1L,
                    Long::sum
            );
        }

        Map<Long, List<BusyPeriod>> busyPeriodsByEmployee =
                montarPeriodosOcupados(appointmentsForDay);

        LocalDateTime requestedEnd = dateTime.plusMinutes(durationMinutes);

        EmployeeEntity selectedEmployee = employees.stream()
                .filter(employee -> !possuiSobreposicao(
                        busyPeriodsByEmployee,
                        employee.getId(),
                        dateTime,
                        requestedEnd
                ))
                .min(Comparator
                        .comparingLong((EmployeeEntity employee) ->
                                appointmentCountByEmployee.get(employee.getId()))
                        .thenComparing(employee -> employee.getUser().getName()))
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Não há profissional disponível nesse horário."
                ));

        return new AvailableEmployeeResponse(
                selectedEmployee.getId(),
                selectedEmployee.getUser().getName()
        );
    }

    /**
     * Monta os intervalos ocupados de cada profissional.

     * A duração é calculada somando os serviços de cada pet e usando
     * a maior duração entre os pets, pois os pets de um mesmo agendamento
     * são considerados atendidos em paralelo.

     * Agendamentos legados sem itens associados são considerados slots
     * de 30 minutos para não deixar de bloquear o horário reservado.
     */
    private Map<Long, List<BusyPeriod>> montarPeriodosOcupados(
            List<Appointments> appointments
    ) {

        Map<Long, Map<Long, Integer>> duracaoPorPetEAgendamento =
                new HashMap<>();

        if (appointmentsPetOfferingRepository != null && !appointments.isEmpty()) {

            List<Long> appointmentIds = appointments.stream()
                    .map(Appointments::getId)
                    .filter(id -> id != null)
                    .toList();

            if (!appointmentIds.isEmpty()) {

                List<AppointmentsPetOffering> itens =
                        appointmentsPetOfferingRepository
                                .findByAppointments_IdIn(appointmentIds);

                for (AppointmentsPetOffering item : itens) {

                    if (item.getAppointment() == null
                            || item.getAppointment().getId() == null
                            || item.getPet() == null
                            || item.getPet().getId() == null
                            || item.getOffering() == null) {
                        continue;
                    }

                    Integer duracaoServico =
                            item.getOffering().getDuracaoMinutos();

                    if (duracaoServico == null || duracaoServico < 0) {
                        continue;
                    }

                    Long appointmentId = item.getAppointment().getId();
                    Long petId = item.getPet().getId();

                    duracaoPorPetEAgendamento
                            .computeIfAbsent(
                                    appointmentId,
                                    key -> new HashMap<>()
                            )
                            .merge(petId, duracaoServico, Integer::sum);
                }
            }
        }

        Map<Long, List<BusyPeriod>> resultado = new HashMap<>();

        for (Appointments appointment : appointments) {

            if (appointment.getId() == null
                    || appointment.getEmployee() == null
                    || appointment.getDateTimeAppointment() == null) {
                continue;
            }

            Map<Long, Integer> duracoesDosPets =
                    duracaoPorPetEAgendamento.getOrDefault(
                            appointment.getId(),
                            Map.of()
                    );

            int maiorDuracao = duracoesDosPets.values().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(30);

            int duracaoFinal = Math.max(30, maiorDuracao);

            LocalDateTime inicio = appointment.getDateTimeAppointment();
            LocalDateTime fim = inicio.plusMinutes(duracaoFinal);

            resultado
                    .computeIfAbsent(
                            appointment.getEmployee().getId(),
                            key -> new ArrayList<>()
                    )
                    .add(new BusyPeriod(inicio, fim));
        }

        return resultado;
    }

    private boolean possuiSobreposicao(
            Map<Long, List<BusyPeriod>> periodosPorFuncionario,
            Long employeeId,
            LocalDateTime inicioSolicitado,
            LocalDateTime fimSolicitado
    ) {

        return periodosPorFuncionario
                .getOrDefault(employeeId, List.of())
                .stream()
                .anyMatch(periodo ->
                        inicioSolicitado.isBefore(periodo.fim())
                                && fimSolicitado.isAfter(periodo.inicio())
                );
    }

    private record BusyPeriod(
            LocalDateTime inicio,
            LocalDateTime fim
    ) {
    }

    private void validarDuracao(Integer durationMinutes) {

        if (durationMinutes == null || durationMinutes < 1 || durationMinutes > 720) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A duração deve estar entre 1 e 720 minutos."
            );
        }
    }

    private EmployeeEntity buscarFuncionario(
            Authentication authentication
    ) {

        EmployeeEntity employee = employeeRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));

        if (!employee.getUser().isActive()) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Funcionário inativo."
            );
        }

        if (employee.getStore() == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Funcionário sem unidade vinculada."
            );
        }

        return employee;
    }

    private StoreEntity buscarUnidade(Long storeId) {

        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Unidade não encontrada."
                ));
    }
}