package com.github.banhosup.dto;

public record DashboardResponse(
        long agendamentosHoje,
        long solicitacoesPendentes,
        long servicosCadastrados,
        long membrosEquipe
) {
}