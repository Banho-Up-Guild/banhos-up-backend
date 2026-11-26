package com.github.banhosup.controller;

import com.github.banhosup.dto.EmployeePermissionsResponse;
import com.github.banhosup.service.EmployeePermissionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/me")
@RequiredArgsConstructor
public class EmployeePermissionController {

    private final EmployeePermissionService permissionService;

    @GetMapping("/permissions")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public EmployeePermissionsResponse minhasPermissoes(
            Authentication authentication
    ) {

        return permissionService.buscarPermissoesDoUsuario(
                authentication
        );
    }
}