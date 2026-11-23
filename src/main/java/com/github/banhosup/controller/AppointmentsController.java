package com.github.banhosup.controller;

import com.github.banhosup.dto.appointments.request.AppointmentsRequestCalendaryDTO;
import com.github.banhosup.dto.appointments.request.AppointmentsRequestDTO;
import com.github.banhosup.dto.appointments.response.AppointmentsResponseCalendaryDTO;
import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.service.AppointmentsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentsController {

    private final AppointmentsService appointmentsService;

    public AppointmentsController(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @PostMapping
    public ResponseEntity<Appointments> createAppointments(@Valid @RequestBody AppointmentsRequestDTO dto){
        Appointments newAppointment = appointmentsService.saveAppointment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAppointment);
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
