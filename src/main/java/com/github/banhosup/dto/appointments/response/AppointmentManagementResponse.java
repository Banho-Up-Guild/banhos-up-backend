package com.github.banhosup.dto.appointments.response;

import com.github.banhosup.model.enums.StatusAppointments;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AppointmentManagementResponse(
        Long id,
        LocalDateTime dateTimeAppointment,
        StatusAppointments status,
        String observation,
        BigDecimal totalPrice,
        Long employeeId,
        String employeeName,
        String customerName,
        String customerEmail,
        List<AppointmentItemResponse> items
) {
}