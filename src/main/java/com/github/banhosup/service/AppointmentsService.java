package com.github.banhosup.service;

import com.github.banhosup.dto.AppointmentsRequestDTO;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.repository.AppointmentsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;

    public AppointmentsService(AppointmentsRepository appointmentsRepository) {
    this.appointmentsRepository = appointmentsRepository;
    }

    public Appointments saveAppointment(AppointmentsRequestDTO dto) {
        Appointments appointment = new Appointments(dto.dateTimeAppointment(), dto.status(), dto.observation());
        return appointmentsRepository.save(appointment);
    }

    public List<Appointments> Alllist() {
        return appointmentsRepository.findAll();
    }
}
