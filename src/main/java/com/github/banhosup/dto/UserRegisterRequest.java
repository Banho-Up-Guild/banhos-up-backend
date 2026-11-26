package com.github.banhosup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import jakarta.validation.constraints.Past;

public record UserRegisterRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "O E-mail é obrigatório.")
        @Email(message = "Digite um formato de E-mail válido.")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "A senha precisa ter letra maiúscula, minúscula, número e caractere especial."
        )
        String password,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", // valida o telefone
                message = "Formato de telefone inválido"
        )
        String phone,

        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(
                regexp = "^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$",
                message = "O CPF deve ter 11 dígitos."
        )
        String cpf,

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
        LocalDate dateOfBirth

) {
}
