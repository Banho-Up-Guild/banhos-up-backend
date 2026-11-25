package com.github.banhosup.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoreRequest(

        @NotBlank(message = "O nome da loja é obrigatório.")
        @Size(max = 100)
        String nome,

        @NotBlank(message = "O CEP é obrigatório.")
        @Pattern(
                regexp = "^\\d{5}-?\\d{3}$",
                message = "CEP inválido."
        )
        String cep,

        @NotBlank(message = "O estado é obrigatório.")
        @Pattern(
                regexp = "^[A-Za-z]{2}$",
                message = "Informe a sigla do estado com 2 letras."
        )
        String estado,

        @NotBlank(message = "A cidade é obrigatória.")
        @Size(max = 100)
        String cidade,

        @NotBlank(message = "O endereço é obrigatório.")
        @Size(max = 150)
        String endereco,

        @NotBlank(message = "O bairro é obrigatório.")
        @Size(max = 100)
        String bairro

) {}