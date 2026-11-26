package com.github.banhosup.dto.dashboard;

public record DashboardResponse(
        long agendamentosHoje,
        long solicitacoesPendentes,
        long servicosCadastrados,
        long membrosEquipe
) {
}