package com.github.banhosup.dto.availability;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StoreAvailabilityRequest(

        @NotEmpty
        @Size(min = 7, max = 7)
        List<@Valid StoreAvailabilityDayRequest> days

) {
}