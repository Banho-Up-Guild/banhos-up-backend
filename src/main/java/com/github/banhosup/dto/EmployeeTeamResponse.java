package com.github.banhosup.dto;

public record EmployeeTeamResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String matricula,
        String cargo,
        Long storeId,
        String nomeUnidade,
        boolean ativo
) {
}