package com.github.banhosup.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Informe seu CPF ou e-mail.")
        String identifier,

        @NotBlank(message = "A senha é obrigatória.")
        String password

) {}