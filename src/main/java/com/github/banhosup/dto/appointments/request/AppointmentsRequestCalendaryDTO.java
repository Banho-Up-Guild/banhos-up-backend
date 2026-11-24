package com.github.banhosup.dto.appointments.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;

public record AppointmentsRequestCalendaryDTO(
        @NotNull(message = "YearMounth is required")
        @DateTimeFormat(pattern = "yyyy-MM")
        YearMonth yearMonth,
        Long employeeId,
        Long storeId
) {}
