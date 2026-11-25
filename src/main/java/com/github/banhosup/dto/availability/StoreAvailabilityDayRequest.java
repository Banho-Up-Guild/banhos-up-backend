package com.github.banhosup.dto.availability;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record StoreAvailabilityDayRequest(

        @NotNull
        @Min(1)
        @Max(7)
        Integer dayOfWeek,

        @NotNull
        Boolean closed,

        LocalTime openingTime,

        LocalTime closingTime,

        LocalTime breakStart,

        LocalTime breakEnd

) {
}