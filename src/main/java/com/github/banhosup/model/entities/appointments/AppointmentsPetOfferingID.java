package com.github.banhosup.model.entities.appointments;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AppointmentsPetOfferingID implements Serializable {

    private Long appointmentId;
    private Long petId;
    private Long offeringId;

    public AppointmentsPetOfferingID() {
    }

    public AppointmentsPetOfferingID(Long appointmentId, Long petId, Long offeringId) {
        this.appointmentId = appointmentId;
        this.petId = petId;
        this.offeringId = offeringId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppointmentsPetOfferingID that)) return false;

        return Objects.equals(appointmentId, that.appointmentId) &&
                Objects.equals(petId, that.petId) &&
                Objects.equals(offeringId, that.offeringId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentId, petId, offeringId);
    }
}
