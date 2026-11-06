package com.github.banhosup.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UserRegisterRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String name,

        @NotBlank(message = "O E-mail é obrigatório.")
        @Email(message = "Digite um fomato de E-mail válido.")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String password,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$", // valida o telfone
                message = "Formato de telefone inválido"
        )
        String phone,

        @NotBlank(message = "O CPF é obrigatório")
        @CPF(message = "CPF inválido.")

        String cpf) {
}
