package com.github.banhosup.model.entities.appointments;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AppointmentsPetServicesID implements Serializable {

    private int appointmentId;
    private int petId;
    private int serviceId;

    public AppointmentsPetServicesID() {
    }

    public AppointmentsPetServicesID(int appointmentId) {
        this.appointmentId = appointmentId;
        this.petId = 1;
        this.serviceId = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppointmentsPetServicesID that)) return false;

        return Objects.equals(appointmentId, that.appointmentId) &&
                Objects.equals(petId, that.petId) &&
                Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appointmentId, petId, serviceId);
    }
}
