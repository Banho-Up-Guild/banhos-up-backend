package com.github.banhosup.dto.appointments.request;

import com.github.banhosup.model.enums.StatusAppointments;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusRequest(

        @NotNull(message = "O status é obrigatório.")
        StatusAppointments status

) {
}