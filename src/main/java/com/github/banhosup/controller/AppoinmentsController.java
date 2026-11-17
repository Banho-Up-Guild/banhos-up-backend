package com.github.banhosup.controller;

import com.github.banhosup.dto.AppointmentsRequestDTO;
import com.github.banhosup.model.entities.Appointments;
import com.github.banhosup.service.AppoinmentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppoinmentsController {

    private final AppoinmentsService appoinmentsService;

    public AppoinmentsController(AppoinmentsService appoinmentsService) {
        this.appoinmentsService = appoinmentsService;
    }

    @PostMapping
    public ResponseEntity<Appointments> createAppointments(@RequestBody AppointmentsRequestDTO dto){
        Appointments newAppointment = appoinmentsService.saveAppointment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAppointment);
    }

    @GetMapping
    public ResponseEntity<List<Appointments>> getAllAppointments(){
        return ResponseEntity.ok(appoinmentsService.Alllist());
    }
}
