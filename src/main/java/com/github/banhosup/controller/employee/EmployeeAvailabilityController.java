package com.github.banhosup.controller.employee;

import com.github.banhosup.dto.availability.StoreAvailabilityDayResponse;
import com.github.banhosup.dto.availability.StoreAvailabilityRequest;
import com.github.banhosup.service.appoiintment.AvailabilityService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee/me/availability")
@RequiredArgsConstructor
@PreAuthorize("@employeePermissionService.hasPermission(authentication, 'AVAILABILITY_MANAGE')")
public class EmployeeAvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<StoreAvailabilityDayResponse>> consultar(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                availabilityService.listarMinhaUnidade(authentication)
        );
    }

    @PutMapping
    public ResponseEntity<List<StoreAvailabilityDayResponse>> salvar(
            @RequestBody @Valid StoreAvailabilityRequest request,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                availabilityService.salvarMinhaUnidade(
                        authentication,
                        request
                )
        );
    }
}