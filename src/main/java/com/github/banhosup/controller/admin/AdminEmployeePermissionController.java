package com.github.banhosup.controller.admin;

import com.github.banhosup.dto.employee.EmployeePermissionsRequest;
import com.github.banhosup.dto.employee.EmployeePermissionsResponse;
import com.github.banhosup.service.employee.EmployeePermissionService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
public class AdminEmployeePermissionController {

    private final EmployeePermissionService permissionService;


    // Consulta as permissões de um funcionário.
    @GetMapping("/{employeeId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeePermissionsResponse buscarPermissoes(
            @PathVariable Long employeeId
    ) {

        return permissionService.buscarPermissoes(employeeId);
    }


    // Atualiza as permissões de um funcionário.
    @PutMapping("/{employeeId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeePermissionsResponse atualizarPermissoes(
            @PathVariable Long employeeId,
            @RequestBody @Valid EmployeePermissionsRequest request
    ) {

        return permissionService.atualizarPermissoes(
                employeeId,
                request
        );
    }

}