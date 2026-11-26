package com.github.banhosup.controller.employee;

import com.github.banhosup.dto.dashboard.DashboardResponse;
import com.github.banhosup.service.dashboard.DashboardService;

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
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<DashboardResponse> resumo(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                dashboardService.resumoFuncionario(authentication)
        );
    }
}