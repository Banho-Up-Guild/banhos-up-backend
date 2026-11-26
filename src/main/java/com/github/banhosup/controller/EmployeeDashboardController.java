package com.github.banhosup.controller;

import com.github.banhosup.dto.DashboardResponse;
import com.github.banhosup.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/me/dashboard")
@RequiredArgsConstructor
public class EmployeeDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'APPOINTMENTS_VIEW')")
    public ResponseEntity<DashboardResponse> resumo(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                dashboardService.resumoFuncionario(authentication)
        );
    }
}