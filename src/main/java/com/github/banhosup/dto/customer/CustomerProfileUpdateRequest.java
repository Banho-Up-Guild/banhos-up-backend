package com.github.banhosup.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerProfileUpdateRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "O celular é obrigatório.")
        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
                message = "Formato de celular inválido."
        )
        String phone,

        @Pattern(
                regexp = "^$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
                message = "Formato de telefone secundário inválido."
        )
        String secondaryPhone,

        @Pattern(
                regexp = "^(feminino|masculino|nao-informar)?$",
                message = "Gênero informado inválido."
        )
        String gender,

        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dateOfBirth,

        @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres.")
        String address
) {
}