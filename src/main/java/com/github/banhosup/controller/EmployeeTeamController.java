package com.github.banhosup.controller;

import com.github.banhosup.dto.EmployeeTeamPositionRequest;
import com.github.banhosup.dto.EmployeeTeamResponse;
import com.github.banhosup.service.EmployeeTeamService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee/me/team")
@RequiredArgsConstructor
public class EmployeeTeamController {

    private final EmployeeTeamService teamService;

    @GetMapping
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'TEAM_MANAGE')")
    public ResponseEntity<List<EmployeeTeamResponse>> listarEquipe(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                teamService.listarMinhaEquipe(authentication)
        );
    }

    @GetMapping("/available")
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'TEAM_MANAGE')")
    public ResponseEntity<List<EmployeeTeamResponse>> listarDisponiveis(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                teamService.listarFuncionariosDisponiveis(authentication)
        );
    }

    @PostMapping("/{employeeId}")
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'TEAM_MANAGE')")
    public ResponseEntity<EmployeeTeamResponse> vincular(
            @PathVariable Long employeeId,
            Authentication authentication
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.vincularFuncionario(
                        employeeId,
                        authentication
                ));
    }

    @PatchMapping("/{employeeId}")
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'TEAM_MANAGE')")
    public ResponseEntity<EmployeeTeamResponse> atualizarCargo(
            @PathVariable Long employeeId,
            @RequestBody @Valid EmployeeTeamPositionRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                teamService.atualizarCargo(
                        employeeId,
                        request,
                        authentication
                )
        );
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'TEAM_MANAGE')")
    public ResponseEntity<Void> desvincular(
            @PathVariable Long employeeId,
            Authentication authentication
    ) {

        teamService.desvincularFuncionario(
                employeeId,
                authentication
        );

        return ResponseEntity.noContent().build();
    }
}