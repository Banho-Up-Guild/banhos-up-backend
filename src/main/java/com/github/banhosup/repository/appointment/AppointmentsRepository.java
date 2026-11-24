package com.github.banhosup.repository.appointment;

import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.enums.StatusAppointments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointments, Long> {

    @Query("SELECT a FROM Appointments a WHERE a.employee.store.id = :storeId AND a.dateTimeAppointment BETWEEN :start AND :end AND a.status != :status")
    List<Appointments> findAvailableSlotsByStore(
            @Param("storeId") Long storeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") StatusAppointments status
    );

    @Query("SELECT a FROM Appointments a WHERE a.employee.id = :employeeId AND a.dateTimeAppointment BETWEEN :start AND :end AND a.status != :status")
    List<Appointments> findAvailableSlotsByEmployee(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") StatusAppointments status
    );

    boolean existsByDateTimeAppointmentAndStatusNot(LocalDateTime appointment, StatusAppointments status);
}
