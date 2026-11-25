package com.github.banhosup.controller;

import com.github.banhosup.dto.availability.AvailabilityCalendarDayResponse;
import com.github.banhosup.service.AvailabilityService;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}