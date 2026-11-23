package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.appointments.request.AppointmentsRequestCalendaryDTO;
import com.github.banhosup.dto.appointments.request.AppointmentsRequestDTO;
import com.github.banhosup.dto.appointments.response.AppointmentsResponseCalendaryDTO;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.repository.AppointmentsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    public AppointmentsService(AppointmentsRepository appointmentsRepository) {
        this.appointmentsRepository = appointmentsRepository;
    }

    // Post de agendamento
    public Appointments saveAppointment(AppointmentsRequestDTO dto) {
        Appointments appointment = new Appointments(dto.dateTimeAppointment(), dto.status(), dto.observation());
        if(appointmentsRepository.existsByDateTimeAppointmentAndStatusNot(dto.dateTimeAppointment(), StatusAppointments.CANCELED)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Esse horário já foi agendado por outro usuário");
        } else {
            return appointmentsRepository.save(appointment);
        }
    }

    // Get de agendamentos
    public List<Appointments> Alllist() {
        return appointmentsRepository.findAll();
    }

    // Get de agendamentos do mês (calendário)
    public List<AppointmentsResponseCalendaryDTO> appointmentsCalendaryList(AppointmentsRequestCalendaryDTO dto) {
        LocalDateTime start = dto.yearMonth().equals(YearMonth.now()) ? LocalDateTime.now() : dto.yearMonth().atDay(1).atStartOfDay();
        LocalDateTime end = dto.yearMonth().atEndOfMonth().atTime(LocalTime.MAX);
        List<Appointments> appointments = appointmentsRepository.findByDateTimeAppointmentBetweenAndStatusNot(start, end, StatusAppointments.CANCELED);
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