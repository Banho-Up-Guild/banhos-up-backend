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

@Service
public class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;
    private final PetRepository petRepository;
    private final OfferingRepository offeringRepository;
    private final AppointmentsPetOfferingRepository appointmentsPetOfferingRepository;
    private final EmployeeRepository employeeRepository;

    public AppointmentsService(AppointmentsRepository appointmentsRepository, PetRepository petRepository, OfferingRepository offeringRepository, AppointmentsPetOfferingRepository appointmentsPetOfferingRepository, EmployeeRepository employeeRepository) {
        this.appointmentsRepository = appointmentsRepository;
        this.petRepository = petRepository;
        this.offeringRepository = offeringRepository;
        this.appointmentsPetOfferingRepository = appointmentsPetOfferingRepository;
        this.employeeRepository = employeeRepository;
    }


    // Post de agendamento
    public Appointments saveAppointment(AppointmentsRequestDTO dto) {
        if(appointmentsRepository.existsByDateTimeAppointmentAndStatusNot(dto.dateTimeAppointment(), StatusAppointments.CANCELED)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Esse horário já foi agendado por outro usuário");
        }

        BigDecimal totalPrice = new BigDecimal(0);
        List<AppointmentsPetOffering> associations = new ArrayList<>();

        for(PetOfferingDTO petOffering : dto.petOffering()) {
            Pet pet = petRepository.findById(petOffering.petId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Pet não encontrado"));
            for(Long offeringID : petOffering.offeringIDs()) {
                Offering offering = offeringRepository.findById(offeringID)
                        .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

                    totalPrice = totalPrice.add(offering.getPreco());

                    associations.add(new AppointmentsPetOffering(null, pet, offering));
            }

        }
        EmployeeEntity employee = employeeRepository.findById(dto.employeeId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));

        Appointments appointments = new Appointments(
                dto.dateTimeAppointment(),
                dto.status(),
                dto.observation(),
                totalPrice,
                employee
        );
        appointments = appointmentsRepository.save(appointments);

        for(AppointmentsPetOffering aps : associations) {
            aps.setAppointment(appointments);
            appointmentsPetOfferingRepository.save(aps);
        }

        return appointments;
    }

    // Get de agendamentos
    public List<Appointments> Alllist() {
        return appointmentsRepository.findAll();
    }

    // Get de agendamentos do mês (calendário)
    public List<AppointmentsResponseCalendaryDTO> appointmentsCalendaryList(AppointmentsRequestCalendaryDTO dto) {
        LocalDateTime start = dto.yearMonth().equals(YearMonth.now()) ? LocalDateTime.now() : dto.yearMonth().atDay(1).atStartOfDay();
        LocalDateTime end = dto.yearMonth().atEndOfMonth().atTime(LocalTime.MAX);

        List<Appointments> appointments = dto.employeeId() == 0 ? appointmentsRepository.findAvailableSlotsByStore(dto.storeId(), start, end, StatusAppointments.CANCELED)
                : appointmentsRepository.findAvailableSlotsByEmployee(dto.employeeId(),start, end, StatusAppointments.CANCELED);

        return appointments.stream()
                .collect(Collectors.groupingBy(
                        app -> app.getDateTimeAppointment().toLocalDate(),
                        TreeMap::new, // Keeps dates sorted in chronological order
                        Collectors.mapping(
                                app -> app.getDateTimeAppointment().toLocalTime().truncatedTo(ChronoUnit.MINUTES),
                                Collectors.toList()
                        )
                ))
                .entrySet().stream()
                .map(entry -> new AppointmentsResponseCalendaryDTO(entry.getKey(), entry.getValue()))
                .toList();
    }
}