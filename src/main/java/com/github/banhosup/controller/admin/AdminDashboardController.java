package com.github.banhosup.controller.admin;

import com.github.banhosup.dto.dashboard.DashboardResponse;
import com.github.banhosup.service.dashboard.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> resumo(
            @RequestParam(required = false) Long storeId
    ) {

        return ResponseEntity.ok(
                dashboardService.resumoAdmin(storeId)
        );
    }
}