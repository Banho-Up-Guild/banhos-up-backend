package com.github.banhosup.dto.appointments.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PetOfferingDTO(
        @NotNull Long petId,
        @NotEmpty(message = "A lista de serviços não pode estar vazia.")
        @JsonProperty("offeringIds")
        List<@NotNull Long> offeringIDs
) {
}
