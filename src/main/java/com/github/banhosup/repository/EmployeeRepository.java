package com.github.banhosup.repository;

import com.github.banhosup.model.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    boolean existsByRegistry(String registry);
}