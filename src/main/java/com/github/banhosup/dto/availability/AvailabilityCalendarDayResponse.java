package com.github.banhosup.dto.availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityCalendarDayResponse(

        LocalDate date,

        List<LocalTime> hours

) {
}