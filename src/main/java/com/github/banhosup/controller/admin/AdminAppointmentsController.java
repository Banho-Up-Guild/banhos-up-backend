package com.github.banhosup.controller.admin;

import com.github.banhosup.dto.appointments.request.AppointmentStatusRequest;
import com.github.banhosup.dto.appointments.response.AppointmentManagementResponse;
import com.github.banhosup.service.appoiintment.AppointmentManagementService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAppointmentsController {

    private final AppointmentManagementService service;


    @GetMapping
    public ResponseEntity<List<AppointmentManagementResponse>> listar(
            @RequestParam(required = false) Long storeId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            com.github.banhosup.model.enums.StatusAppointments status
    ) {

        return ResponseEntity.ok(
                service.listarAdmin(
                        storeId,
                        startDate,
                        endDate,
                        status
                )
        );
    }


    @GetMapping("/requests")
    public ResponseEntity<List<AppointmentManagementResponse>> listarSolicitacoes() {

        return ResponseEntity.ok(
                service.listarAdmin(
                        null,
                        null,
                        null,
                        com.github.banhosup.model.enums.StatusAppointments.AWAITING_CONFIRMATION
                )
        );
    }


    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentManagementResponse> atualizarStatus(
            @PathVariable Long appointmentId,
            @RequestBody @Valid AppointmentStatusRequest request
    ) {

        return ResponseEntity.ok(
                service.atualizarStatusAdmin(
                        appointmentId,
                        request.status()
                )
        );
    }
}