package com.github.banhosup.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.banhosup.model.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRegisterRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "O E-mail é obrigatório.")
        @Email(message = "Digite um formato de E-mail válido.")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
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

        @NotNull(message="O Gênero é obrigatório")
        Gender gender,

        @NotNull(message = "A data de nascimento é obrigatória")
        @Past(message = "A data de nascimento deve estar no passado")
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate birthDate
)
{}
