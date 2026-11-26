package com.github.banhosup.controller.employee;

import com.github.banhosup.dto.appointments.response.AppointmentManagementResponse;
import com.github.banhosup.model.enums.StatusAppointments;
import com.github.banhosup.service.appoiintment.AppointmentManagementService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/employee/me/appointments")
@RequiredArgsConstructor
public class EmployeeAppointmentsController {

    private final AppointmentManagementService service;


    @GetMapping
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'APPOINTMENTS_VIEW')")
    public ResponseEntity<List<AppointmentManagementResponse>> listar(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            StatusAppointments status
    ) {

        return ResponseEntity.ok(
                service.listarFuncionario(
                        authentication,
                        startDate,
                        endDate,
                        status
                )
        );
    }


    @GetMapping("/requests")
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'APPOINTMENTS_VIEW')")
    public ResponseEntity<List<AppointmentManagementResponse>> listarSolicitacoes(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                service.listarFuncionario(
                        authentication,
                        null,
                        null,
                        StatusAppointments.AWAITING_CONFIRMATION
                )
        );
    }


    @PatchMapping("/{appointmentId}/confirm")
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'APPOINTMENTS_CONFIRM')")
    public ResponseEntity<AppointmentManagementResponse> confirmar(
            @PathVariable Long appointmentId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                service.atualizarStatusFuncionario(
                        appointmentId,
                        StatusAppointments.CONFIRMED,
                        authentication
                )
        );
    }


    @PatchMapping("/{appointmentId}/reject")
    @PreAuthorize("@employeePermissionService.hasPermission(authentication, 'APPOINTMENTS_CONFIRM')")
    public ResponseEntity<AppointmentManagementResponse> recusar(
            @PathVariable Long appointmentId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                service.atualizarStatusFuncionario(
                        appointmentId,
                        StatusAppointments.CANCELED,
                        authentication
                )
        );
    }
}