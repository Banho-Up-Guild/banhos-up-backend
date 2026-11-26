package com.github.banhosup.controller;

import com.github.banhosup.dto.appointments.request.AppointmentsRequestCalendaryDTO;
import com.github.banhosup.dto.appointments.request.AppointmentsRequestDTO;
import com.github.banhosup.dto.appointments.response.AppointmentsResponseCalendaryDTO;
import com.github.banhosup.dto.appointments.response.AppointmentsResponseDTO;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.service.AppointmentsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/appointments")
public class AppointmentsController {

    private final AppointmentsService appointmentsService;

    public AppointmentsController(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @PostMapping
    public ResponseEntity<AppointmentsResponseDTO> createAppointments(
            @Valid @RequestBody AppointmentsRequestDTO dto,
            Authentication authentication
    ) {
        Appointments newAppointment =
                appointmentsService.saveAppointment(dto, authentication);
        AppointmentsResponseDTO response = new AppointmentsResponseDTO(
                newAppointment.getDateTimeAppointment(),
                newAppointment.getStatus(),
                newAppointment.getObservation(),
                newAppointment.getTotalPrice(),
                newAppointment.getEmployee().getId(),
                newAppointment.getEmployee().getUser().getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<Appointments>> getAllAppointments(){
        return ResponseEntity.ok(appointmentsService.Alllist());
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<AppointmentsResponseCalendaryDTO>> getAllAppointmentsByCalendar(
            @Valid AppointmentsRequestCalendaryDTO dto
    ){
        return ResponseEntity.ok(appointmentsService.appointmentsCalendaryList(dto));
    }
}
