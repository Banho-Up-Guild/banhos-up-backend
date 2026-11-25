package com.github.banhosup.repository.appointment;

import com.github.banhosup.model.entities.appointments.AppointmentsPetOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentsPetOfferingRepository extends JpaRepository<AppointmentsPetOffering,Long> {
    List<AppointmentsPetOffering> findByAppointments_Id(
            Long appointmentId
    );
}
