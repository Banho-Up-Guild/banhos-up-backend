package com.github.banhosup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(

        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "O telefone é obrigatório.")
        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", message = "Formato de telefone inválido.")
        String phone
) {
}