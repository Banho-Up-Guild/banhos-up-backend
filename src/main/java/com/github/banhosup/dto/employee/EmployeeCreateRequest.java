package com.github.banhosup.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmployeeCreateRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Digite um formato de e-mail válido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String password,

        @NotBlank(message = "O telefone é obrigatório.")
        @Pattern(regexp = "^\\d{10,11}$", message = "O telefone deve conter 10 ou 11 dígitos.")
        String phone,

        @NotBlank(message = "O CPF é obrigatório.")
        @Pattern(regexp = "^\\d{11}$", message = "O CPF deve conter 11 dígitos.")
        String cpf,

        @NotBlank(message = "A matrícula é obrigatória.")
        @Size(max = 50, message = "A matrícula deve ter no máximo 50 caracteres.")
        String registry,

        @NotBlank(message = "O cargo é obrigatório.")
        @Size(max = 100, message = "O cargo deve ter no máximo 100 caracteres.")
        String position
) {
}
