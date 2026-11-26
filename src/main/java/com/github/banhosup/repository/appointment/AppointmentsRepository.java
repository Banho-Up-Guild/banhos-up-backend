package com.github.banhosup.repository.appointment;

import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.enums.StatusAppointments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointments, Long> {
    List<Appointments> findByDateTimeAppointmentBetweenAndStatusNot(
            LocalDateTime start,
            LocalDateTime end,
            StatusAppointments status
    );

    List<Appointments>
    findByEmployee_Store_IdOrderByDateTimeAppointmentAsc(Long storeId);

    Optional<Appointments> findByIdAndEmployee_Store_Id(
            Long appointmentId,
            Long storeId
    );

    boolean existsByEmployee_IdAndDateTimeAppointmentAndStatusNot(
            Long employeeId,
            LocalDateTime appointment,
            StatusAppointments status
    );

    List<Appointments> findByEmployee_IdInAndDateTimeAppointmentBetweenAndStatusNot(
            List<Long> employeeIds,
            LocalDateTime start,
            LocalDateTime end,
            StatusAppointments status
    );

    List<Appointments> findByEmployee_IdInAndDateTimeAppointmentAfterAndStatusNot(
            List<Long> employeeIds,
            LocalDateTime start,
            StatusAppointments status
    );
}
