package com.github.banhosup.dto.employee;

import com.github.banhosup.model.enums.EmployeePermission;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record EmployeePermissionsRequest(

        @NotNull(message = "A lista de permissões é obrigatória.")
        Set<EmployeePermission> permissions

) {
}