package com.github.banhosup.dto.appointments.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AppointmentsResponseCalendaryDTO(
    LocalDate date,
    List<LocalTime> hours
) {}
