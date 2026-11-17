package com.github.banhosup.repository;

import com.github.banhosup.model.entities.Appointments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointments, Long> {
}
