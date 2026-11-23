package com.github.banhosup.model.entities.appointments;

import com.github.banhosup.model.entities.Offering;
import com.github.banhosup.model.entities.Pet;
import jakarta.persistence.*;

@Entity
@Table(name = "appointments_pet_service")
public class AppointmentsPetOffering {

    @EmbeddedId
    private AppointmentsPetOfferingID id = new AppointmentsPetOfferingID();

    @ManyToOne
    @MapsId("appointmentId")
    @JoinColumn(name = "appointment_id")
    private Appointments appointments;

    @ManyToOne
    @MapsId("petId")
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne
    @MapsId("offeringId")
    @JoinColumn(name = "service_id")
    private Offering offering;


    public AppointmentsPetOffering() {}

    public AppointmentsPetOffering(Appointments appointment, Pet pet, Offering offering){
        this.appointments = appointment;
        this.pet = pet;
        this.offering = offering;
    }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public Offering getOffering() { return offering; }
    public void setOffering(Offering offering) { this.offering = offering; }

    public AppointmentsPetOfferingID getId() { return id; }
    public void setId(AppointmentsPetOfferingID id) { this.id = id; }

    public Appointments getAppointment() { return appointments; }
    public void setAppointment(Appointments appointment) { this.appointments = appointment; }
}
