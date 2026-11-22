package com.github.banhosup.repository;

import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.enums.StatusAppointments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointments, Long> {
    List<Appointments> findByDateTimeAppointmentBetweenAndStatusNot(
            LocalDateTime start,
            LocalDateTime end,
            StatusAppointments status
    );

    boolean existsByDateTimeAppointmentAndStatusNot(LocalDateTime appointment, StatusAppointments status);
}
