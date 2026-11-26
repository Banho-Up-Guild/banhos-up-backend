package com.github.banhosup.controller.employee;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.store.StoreResponse;
import com.github.banhosup.model.entities.employee.EmployeeEntity;
import com.github.banhosup.repository.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.github.banhosup.dto.store.StoreRequest;
import com.github.banhosup.service.store.StoreService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/employee/me")
@RequiredArgsConstructor
public class EmployeeStoreController {

    private final EmployeeRepository employeeRepository;
    private final StoreService storeService;

    @GetMapping("/store")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<StoreResponse> minhaUnidade(
            Authentication authentication
    ) {

        EmployeeEntity employee = employeeRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));

        if (employee.getStore() == null) {
            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Este funcionário não possui uma unidade vinculada."
            );
        }

        return ResponseEntity.ok(
                StoreResponse.from(employee.getStore())
        );
    }

    @PutMapping("/store")
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'STORES_MANAGE')")
    public ResponseEntity<StoreResponse> atualizarMinhaUnidade(
            @RequestBody @Valid StoreRequest request,
            Authentication authentication
    ) {

        EmployeeEntity employee = employeeRepository
                .findByUser_Email(authentication.getName())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Funcionário não encontrado."
                ));

        if (!employee.getUser().isActive() ||
                !employee.isTeamActive()) {

            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Funcionário sem vínculo ativo com a unidade."
            );
        }

        if (employee.getStore() == null) {

            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Funcionário sem unidade vinculada."
            );
        }

        // O ID da unidade vem do funcionário autenticado.
        // Não recebemos storeId do Angular.
        StoreResponse resposta = storeService.atualizar(
                employee.getStore().getId(),
                request
        );

        return ResponseEntity.ok(resposta);
    }
}