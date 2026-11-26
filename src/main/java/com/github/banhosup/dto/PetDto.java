package com.github.banhosup.dto;

import com.github.banhosup.model.enums.Porte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PetDto(
        Long id,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "A espécie é obrigatória")
        @Size(max = 50, message = "A espécie deve ter no máximo 50 caracteres")
        String especie,

        @Size(max = 80, message = "A raça deve ter no máximo 80 caracteres")
        String raca,

        @PastOrPresent(message = "A data de nascimento não pode estar no futuro")
        LocalDate dataNascimento,

        Porte porte,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes,

        Long donoId
) {
}