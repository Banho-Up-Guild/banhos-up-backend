package com.github.banhosup.model.entities.appointments;

import jakarta.persistence.*;

@Entity
@Table(name = "appointments_pet_service")
public class AppointmentsPetServices {

    @EmbeddedId
    private AppointmentsPetServicesID id = new AppointmentsPetServicesID();

    @ManyToOne
    @MapsId("appointmentId")
    @JoinColumn(name = "appointment_id")
    private Appointments appointments;

    /* Aguardando criação de pet e services.

    @ManyToOne
    @MapsId("petId")
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne
    @MapsId("serviceId")
    @JoinColumn(name = "service_id")
    private Service service;

    */

    public AppointmentsPetServices() {}

    public AppointmentsPetServices(Appointments appointment){
        this.appointments = appointment;
        this.id = new AppointmentsPetServicesID(appointment.getId());
    }

    public AppointmentsPetServicesID getId() { return id; }
    public void setId(AppointmentsPetServicesID id) { this.id = id; }

    public Appointments getAppointment() { return appointments; }
    public void setAppointment(Appointments appointment) { this.appointments = appointment; }
}
