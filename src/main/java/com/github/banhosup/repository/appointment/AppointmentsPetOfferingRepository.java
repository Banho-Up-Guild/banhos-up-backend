package com.github.banhosup.repository.appointment;

import com.github.banhosup.model.entities.appointments.AppointmentsPetOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentsPetOfferingRepository extends JpaRepository<AppointmentsPetOffering,Long> {
}
