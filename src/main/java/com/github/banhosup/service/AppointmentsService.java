package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.appointments.request.AppointmentsRequestCalendaryDTO;
import com.github.banhosup.dto.appointments.request.AppointmentsRequestDTO;
import com.github.banhosup.dto.appointments.request.PetOfferingDTO;
import com.github.banhosup.dto.appointments.response.AppointmentsResponseCalendaryDTO;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.model.entities.Offering;
import com.github.banhosup.model.entities.Pet;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.entities.appointments.AppointmentsPetOffering;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.EmployeeRepository;
import com.github.banhosup.repository.appointment.AppointmentsPetOfferingRepository;
import com.github.banhosup.repository.appointment.AppointmentsRepository;
import com.github.banhosup.repository.OfferingRepository;
import com.github.banhosup.repository.PetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.github.banhosup.model.entities.CustomerEntity;
import com.github.banhosup.repository.CustomerRepository;
import com.github.banhosup.dto.availability.AvailabilityCalendarDayResponse;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.Locale;

@Service
public class AppointmentsService {

    private final CustomerRepository customerRepository;
    private final AvailabilityService availabilityService;
    private final AppointmentsRepository appointmentsRepository;
    private final PetRepository petRepository;
    private final OfferingRepository offeringRepository;
    private final AppointmentsPetOfferingRepository appointmentsPetOfferingRepository;
    private final EmployeeRepository employeeRepository;

    public AppointmentsService(
            AppointmentsRepository appointmentsRepository,
            PetRepository petRepository,
            OfferingRepository offeringRepository,
            AppointmentsPetOfferingRepository appointmentsPetOfferingRepository,
            EmployeeRepository employeeRepository,
            CustomerRepository customerRepository,
            AvailabilityService availabilityService
    ) {
        this.appointmentsRepository = appointmentsRepository;
        this.petRepository = petRepository;
        this.offeringRepository = offeringRepository;
        this.appointmentsPetOfferingRepository = appointmentsPetOfferingRepository;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.availabilityService = availabilityService;
    }

    // Post de agendamento
    @Transactional
    public Appointments saveAppointment(
            AppointmentsRequestDTO dto,
            Authentication authentication
    ) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED,
                    "É necessário estar autenticado para agendar."
            );
        }

        if (dto.dateTimeAppointment() == null
                || dto.employeeId() == null
                || dto.petOffering() == null
                || dto.petOffering().isEmpty()) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Informe a data, o profissional, os pets e os serviços."
            );
        }

        // Identifica o cliente pelo usuário autenticado.
        CustomerEntity customer = customerRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.FORBIDDEN,
                        "Apenas clientes podem realizar agendamentos."
                ));

        // Busca o funcionário selecionado.
        EmployeeEntity employee = employeeRepository
                .findByIdForUpdate(dto.employeeId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));

        // Confere se o funcionário está ativo e vinculado a uma unidade.
        if (!employee.getUser().isActive()
                || !employee.isTeamActive()
                || employee.getStore() == null
                || !employee.getStore().isAtiva()) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O profissional não está disponível para agendamentos."
            );
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        List<AppointmentsPetOffering> associations = new ArrayList<>();

        Set<Long> petsUtilizados = new HashSet<>();

        int maiorDuracaoPorPet = 0;

        // Valida os pets e os serviços escolhidos.
        for (PetOfferingDTO petOffering : dto.petOffering()) {

            if (petOffering.petId() == null
                    || petOffering.offeringIDs() == null
                    || petOffering.offeringIDs().isEmpty()) {

                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Cada pet precisa ter pelo menos um serviço selecionado."
                );
            }

            if (!petsUtilizados.add(petOffering.petId())) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "Não repita o mesmo pet no agendamento."
                );
            }

            Pet pet = petRepository.findById(petOffering.petId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND,
                            "Pet não encontrado."
                    ));

            // Impede que um cliente utilize o pet de outra pessoa.
            if (!pet.getDono().getId().equals(customer.getId())) {
                throw new BusinessException(
                        HttpStatus.FORBIDDEN,
                        "Você não pode realizar agendamentos para pets de outro cliente."
                );
            }

            int duracaoPet = 0;
            boolean possuiServicoDeBanho = false;

            Set<Long> servicosUtilizados = new HashSet<>();

            for (Long offeringID : petOffering.offeringIDs()) {

                if (offeringID == null || !servicosUtilizados.add(offeringID)) {
                    throw new BusinessException(
                            HttpStatus.BAD_REQUEST,
                            "Serviço inválido ou repetido para o mesmo pet."
                    );
                }

                if (dto.petOffering().size() > 3) {
                    throw new BusinessException(
                            HttpStatus.BAD_REQUEST,
                            "É permitido agendar no máximo três pets por vez."
                    );
                }

                Offering offering = offeringRepository.findById(offeringID)
                        .orElseThrow(() -> new BusinessException(
                                HttpStatus.NOT_FOUND,
                                "Serviço não encontrado."
                        ));

                if (!Boolean.TRUE.equals(offering.getAtivo())) {
                    throw new BusinessException(
                            HttpStatus.CONFLICT,
                            "O serviço selecionado não está mais disponível."
                    );
                }

                // O Backend usa o preço vigente no banco.
                totalPrice = totalPrice.add(offering.getPreco());

                String nomeServico = offering.getNome();

                if (nomeServico != null
                        && nomeServico.toLowerCase(Locale.ROOT).contains("banho")) {
                    possuiServicoDeBanho = true;
                }

                duracaoPet += offering.getDuracaoMinutos();

                associations.add(
                        new AppointmentsPetOffering(null, pet, offering)
                );
            }

            if (!possuiServicoDeBanho) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "O serviço de banho é obrigatório para cada pet."
                );
            }

            maiorDuracaoPorPet = Math.max(
                    maiorDuracaoPorPet,
                    duracaoPet
            );
        }

        // Os serviços do mesmo pet são somados.
        // Os pets são considerados em paralelo.
        int duracaoTotal = Math.max(30, maiorDuracaoPorPet);

        if (dto.dateTimeAppointment().getSecond() != 0
                || dto.dateTimeAppointment().getNano() != 0) {

            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "O horário deve ser informado com precisão de minutos."
            );
        }

        // Consulta a disponibilidade do profissional escolhido.
        List<AvailabilityCalendarDayResponse> calendario =
                availabilityService.consultarCalendario(
                        employee.getStore().getId(),
                        employee.getId(),
                        YearMonth.from(dto.dateTimeAppointment()),
                        duracaoTotal
                );

        boolean horarioDisponivel = calendario.stream()
                .filter(dia -> dia.date().equals(
                        dto.dateTimeAppointment().toLocalDate()
                ))
                .flatMap(dia -> dia.hours().stream())
                .anyMatch(hora -> hora.equals(
                        dto.dateTimeAppointment().toLocalTime()
                ));

        if (!horarioDisponivel) {
            throw new BusinessException(
                    HttpStatus.CONFLICT,
                    "O horário selecionado não está mais disponível."
            );
        }

        // O status inicial é sempre uma solicitação pendente.
        Appointments appointment = new Appointments(
                dto.dateTimeAppointment(),
                StatusAppointments.AWAITING_CONFIRMATION,
                dto.observation() == null ? "" : dto.observation(),
                totalPrice,
                employee
        );

        appointment.setStore(employee.getStore());

        appointment = appointmentsRepository.save(appointment);

        // Registra os pets e serviços associados ao agendamento.
        for (AppointmentsPetOffering association : associations) {
            association.setAppointment(appointment);
        }

        appointmentsPetOfferingRepository.saveAll(associations);

        return appointment;
    }

    // Get de agendamentos
    public List<Appointments> Alllist() {
        return appointmentsRepository.findAll();
    }

    // Get de agendamentos do mês (calendário)
    public List<AppointmentsResponseCalendaryDTO> appointmentsCalendaryList(
            AppointmentsRequestCalendaryDTO dto
    ) {

        LocalDateTime start = dto.yearMonth().equals(YearMonth.now())
                ? LocalDateTime.now()
                : dto.yearMonth().atDay(1).atStartOfDay();

        LocalDateTime end = dto.yearMonth().atEndOfMonth().atTime(LocalTime.MAX);

        List<Appointments> appointments =
                appointmentsRepository.findByDateTimeAppointmentBetweenAndStatusNot(
                        start,
                        end,
                        StatusAppointments.CANCELED
                );

        return appointments.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getDateTimeAppointment().toLocalDate(),
                        TreeMap::new,
                        Collectors.mapping(
                                app -> app.getDateTimeAppointment()
                                        .toLocalTime()
                                        .truncatedTo(ChronoUnit.MINUTES),
                                Collectors.toList()
                        )
                ))
                .entrySet().stream()
                .map(entry -> new AppointmentsResponseCalendaryDTO(
                        entry.getKey(),
                        entry.getValue()
                ))
                .toList();
    }
}