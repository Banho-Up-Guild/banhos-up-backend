package com.github.banhosup.dto.appointments.response;

import java.math.BigDecimal;

public record AppointmentItemResponse(
        Long petId,
        String petName,
        String species,
        Long offeringId,
        String offeringName,
        BigDecimal offeringPrice
) {
}