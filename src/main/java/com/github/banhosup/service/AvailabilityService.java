package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.availability.AvailabilityCalendarDayResponse;
import com.github.banhosup.dto.availability.StoreAvailabilityDayRequest;
import com.github.banhosup.dto.availability.StoreAvailabilityDayResponse;
import com.github.banhosup.dto.availability.StoreAvailabilityRequest;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.model.entities.StoreBusinessHoursEntity;
import com.github.banhosup.model.entities.StoreEntity;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.entities.appointments.AppointmentsPetOffering;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.EmployeeRepository;
import com.github.banhosup.repository.StoreBusinessHoursRepository;
import com.github.banhosup.repository.StoreRepository;
import com.github.banhosup.repository.appointment.AppointmentsPetOfferingRepository;
import com.github.banhosup.repository.appointment.AppointmentsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private static final int INTERVAL_MINUTES = 30;

    private final StoreBusinessHoursRepository hoursRepository;
    private final StoreRepository storeRepository;
    private final EmployeeRepository employeeRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final AppointmentsPetOfferingRepository itemsRepository;


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


    // CALENDÁRIO PÚBLICO: retorna somente horários disponíveis.
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

        if (employeeId != null) {

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

        Map<Long, List<BusyPeriod>> busyByEmployee =
                new HashMap<>();

        for (Appointments appointment : appointments) {

            int appointmentDuration =
                    calcularDuracaoAgendamento(appointment.getId());

            LocalDateTime start =
                    appointment.getDateTimeAppointment();

            LocalDateTime end =
                    start.plusMinutes(appointmentDuration);

            Long appointmentEmployeeId =
                    appointment.getEmployee().getId();

            busyByEmployee
                    .computeIfAbsent(
                            appointmentEmployeeId,
                            key -> new ArrayList<>()
                    )
                    .add(new BusyPeriod(start, end));
        }

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

                    boolean availableForSomeone =
                            employees.stream().anyMatch(employee -> {

                                List<BusyPeriod> busy =
                                        busyByEmployee.getOrDefault(
                                                employee.getId(),
                                                List.of()
                                        );

                                return busy.stream().noneMatch(period ->
                                        slotStartDateTime.isBefore(period.end())
                                                && slotStartDateTime
                                                .plusMinutes(durationMinutes)
                                                .isAfter(period.start())
                                );
                            });

                    if (availableForSomeone) {
                        availableHours.add(slot);
                    }
                }

                slot = slot.plusMinutes(INTERVAL_MINUTES);
            }

            if (!availableHours.isEmpty()) {

                result.add(new AvailabilityCalendarDayResponse(
                        date,
                        availableHours
                ));
            }
        }

        return result;
    }


    private int calcularDuracaoAgendamento(Long appointmentId) {

        List<AppointmentsPetOffering> items =
                itemsRepository.findByAppointments_Id(appointmentId);

        Map<Long, Integer> durationByPet = new HashMap<>();

        for (AppointmentsPetOffering item : items) {

            Long petId = item.getPet().getId();

            Integer duration =
                    item.getOffering().getDuracaoMinutos();

            if (duration != null) {
                durationByPet.merge(
                        petId,
                        duration,
                        Integer::sum
                );
            }
        }

        // Segue a lógica atual do Frontend:
        // pets são tratados em paralelo e vale a maior duração.
        return Math.max(
                INTERVAL_MINUTES,
                durationByPet.values()
                        .stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(INTERVAL_MINUTES)
        );
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


    private record BusyPeriod(
            LocalDateTime start,
            LocalDateTime end
    ) {
    }
}