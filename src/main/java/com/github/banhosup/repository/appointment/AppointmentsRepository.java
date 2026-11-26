package com.github.banhosup.repository.appointment;

import com.github.banhosup.model.entities.appointments.Appointments;
import com.github.banhosup.model.enums.StatusAppointments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.banhosup.model.entities.appointments.AppointmentsPetOffering;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Appointments> findByEmployee_Store_IdOrderByDateTimeAppointmentAsc(
            Long storeId
    );

    Optional<Appointments> findByIdAndEmployee_Store_Id(
            Long appointmentId,
            Long storeId
    );

    boolean existsByDateTimeAppointmentAndStatusNot(
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
            LocalDateTime after,
            StatusAppointments status
    );

    @Query("""
        SELECT DISTINCT a
        FROM Appointments a
        JOIN AppointmentsPetOffering item ON item.appointments = a
        JOIN item.pet pet
        WHERE pet.dono.id = :customerId
        ORDER BY a.dateTimeAppointment DESC
    """)
    List<Appointments> findAllByCustomerIdOrderByDateTimeAppointmentDesc(
            @Param("customerId") Long customerId
    );

    // Consulta os agendamentos pela unidade registrada no próprio atendimento.
    List<Appointments> findByStore_IdOrderByDateTimeAppointmentAsc(
            Long storeId
    );

    Optional<Appointments> findByIdAndStore_Id(
            Long appointmentId,
            Long storeId
    );

    // Verifica se o funcionário ainda possui compromissos futuros não cancelados.
    boolean existsByEmployee_IdAndDateTimeAppointmentGreaterThanEqualAndStatusNot(
            Long employeeId,
            LocalDateTime dateTime,
            StatusAppointments status
    );
}