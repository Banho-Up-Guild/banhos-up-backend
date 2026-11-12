package com.github.banhosup.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

//o record ServicoDto tem como objetivo validar as requisicoes do usuario para
//que elas sejam propriamente definidas, o correto seria separar para o administrador
//mas por agora ela ficará geral

public record ServicoDto(
        Long id,

        @NotBlank(message = "O nome é obrigatorio")
        @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
        String nome,

        @Size(max = 500, message = "A descricao deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message="O preço é obrigatorio.")
        @DecimalMin(value="0.0", message = "O preco não pode ser negativo.")
        BigDecimal preco,

        @NotNull(message = "A duração é obrigatória")
        @Positive(message = "A duração deve ser maior que zero")
        Integer duracaoMinutos,

        Boolean ativo
) {

}
