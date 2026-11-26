package com.github.banhosup.controller.admin;

import com.github.banhosup.dto.employee.EmployeeTeamPositionRequest;
import com.github.banhosup.dto.employee.EmployeeTeamResponse;
import com.github.banhosup.service.admin.AdminTeamService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/lojas/{storeId}/equipe")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTeamController {

    private final AdminTeamService teamService;


    @GetMapping
    public ResponseEntity<List<EmployeeTeamResponse>> listar(
            @PathVariable Long storeId
    ) {

        return ResponseEntity.ok(
                teamService.listarEquipe(storeId)
        );
    }


    @GetMapping("/disponiveis")
    public ResponseEntity<List<EmployeeTeamResponse>> listarDisponiveis(
            @PathVariable Long storeId
    ) {

        return ResponseEntity.ok(
                teamService.listarDisponiveis(storeId)
        );
    }


    @PostMapping("/{employeeId}")
    public ResponseEntity<EmployeeTeamResponse> adicionar(
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.adicionar(storeId, employeeId));
    }


    @PatchMapping("/{employeeId}")
    public ResponseEntity<EmployeeTeamResponse> atualizarCargo(
            @PathVariable Long storeId,
            @PathVariable Long employeeId,
            @RequestBody @Valid EmployeeTeamPositionRequest request
    ) {

        return ResponseEntity.ok(
                teamService.atualizarCargo(
                        storeId,
                        employeeId,
                        request
                )
        );
    }


    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> remover(
            @PathVariable Long storeId,
            @PathVariable Long employeeId
    ) {

        teamService.remover(storeId, employeeId);

        return ResponseEntity.noContent().build();
    }
}