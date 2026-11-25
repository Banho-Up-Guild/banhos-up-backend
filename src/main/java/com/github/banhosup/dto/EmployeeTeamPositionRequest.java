package com.github.banhosup.dto;

import jakarta.validation.constraints.NotBlank;

public record EmployeeTeamPositionRequest(

        @NotBlank(message = "O cargo é obrigatório.")
        String position

) {
}
