package com.github.banhosup.controller;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.StoreResponse;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/me")
@RequiredArgsConstructor
public class EmployeeStoreController {

    private final EmployeeRepository employeeRepository;

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
}