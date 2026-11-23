package com.github.banhosup.dto.appointments.response;

import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.model.enums.StatusAppointments;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentsResponseDTO(
        LocalDateTime dateTimeAppointment,
        StatusAppointments status,
        String observation,
        BigDecimal totalPrice,
        Long idEmployee,
        String nameEmployee
) {
}
