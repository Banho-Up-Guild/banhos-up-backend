package com.github.banhosup.dto.availability;

import java.time.LocalTime;

public record StoreAvailabilityDayResponse(

        Integer dayOfWeek,

        String dayName,

        boolean closed,

        LocalTime openingTime,

        LocalTime closingTime,

        LocalTime breakStart,

        LocalTime breakEnd

) {
}