package com.github.banhosup.controller;

import com.github.banhosup.dto.appointments.response.AppointmentManagementResponse;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.service.AppointmentManagementService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/me/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerAppointmentsController {

    private final AppointmentManagementService service;

    @GetMapping
    public ResponseEntity<List<AppointmentManagementResponse>> listar(
            Authentication authentication,
            @RequestParam(required = false) StatusAppointments status
    ) {

        return ResponseEntity.ok(
                service.listarCliente(authentication, status)
        );
    }
}