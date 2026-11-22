package com.github.banhosup.dto.appointments.request;

import com.github.banhosup.model.enums.StatusAppointments;

import java.time.LocalDateTime;

public record AppointmentsRequestDTO(
        LocalDateTime dateTimeAppointment,
        StatusAppointments status,
        String observation
)
{}
