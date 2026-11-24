package com.github.banhosup.repository;

import com.github.banhosup.model.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    boolean existsByRegistry(String registry);

    List<EmployeeEntity> findByStoreId(Long idStore);
}