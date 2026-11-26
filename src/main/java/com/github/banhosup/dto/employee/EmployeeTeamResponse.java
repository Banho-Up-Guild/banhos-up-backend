package com.github.banhosup.dto.employee;

import com.github.banhosup.model.entities.employee.EmployeeEntity;

public record EmployeeTeamResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        String matricula,
        String cargo,
        Long storeId,
        String nomeUnidade,
        boolean ativo,
        boolean teamActive
) {

    public static EmployeeTeamResponse from(EmployeeEntity employee) {

        return new EmployeeTeamResponse(
                employee.getId(),
                employee.getUser().getName(),
                employee.getUser().getEmail(),
                employee.getUser().getPhone(),
                employee.getRegistry(),
                employee.getPosition(),
                employee.getStore() != null
                        ? employee.getStore().getId()
                        : null,
                employee.getStore() != null
                        ? employee.getStore().getNome()
                        : null,
                employee.getUser().isActive(),
                employee.isTeamActive()
        );
    }
}