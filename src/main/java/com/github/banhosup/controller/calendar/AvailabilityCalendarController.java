package com.github.banhosup.controller.calendar;

import com.github.banhosup.dto.availability.AvailabilityCalendarDayResponse;
import com.github.banhosup.dto.availability.AvailableEmployeeResponse;
import com.github.banhosup.service.appoiintment.AvailabilityService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
public class AvailabilityCalendarController {

    private final AvailabilityService availabilityService;

    @GetMapping("/calendar")
    public ResponseEntity<List<AvailabilityCalendarDayResponse>> consultar(
            @RequestParam Long storeId,

            @RequestParam(required = false) Long employeeId,

            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth yearMonth,

            @RequestParam Integer durationMinutes
    ) {

        return ResponseEntity.ok(
                availabilityService.consultarCalendario(
                        storeId,
                        employeeId,
                        yearMonth,
                        durationMinutes
                )
        );
    }

    @GetMapping("/employee")
    public ResponseEntity<AvailableEmployeeResponse> selecionarFuncionario(
            @RequestParam Long storeId,

            @RequestParam(required = false) Long employeeId,

            @RequestParam
            @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm")
            LocalDateTime dateTime,

            @RequestParam(defaultValue = "30") Integer durationMinutes
    ) {

        return ResponseEntity.ok(
                availabilityService.selecionarFuncionario(
                        storeId,
                        employeeId,
                        dateTime,
                        durationMinutes
                )
        );
    }
}