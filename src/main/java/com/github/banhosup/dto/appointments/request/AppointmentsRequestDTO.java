package com.github.banhosup.dto.appointments.request;

import com.github.banhosup.model.enums.StatusAppointments;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record AppointmentsRequestDTO(
        LocalDateTime dateTimeAppointment,
        StatusAppointments status,
        String observation,
        @NotNull(message = "O funcionário é obrigatório.")
        Long employeeId,
        @Valid
        @NotEmpty(message = "A lista de pets e serviços não pode estar vazia.")
        List<@NotNull PetOfferingDTO> petOffering

)
{}
