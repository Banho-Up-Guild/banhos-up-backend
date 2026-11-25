package com.github.banhosup.repository;

import com.github.banhosup.model.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    boolean existsByRegistry(String registry);

    Optional<EmployeeEntity> findByUser_Email(String email);

    List<EmployeeEntity> findByStore_Id(Long storeId);

    List<EmployeeEntity>
    findByStore_IdAndUser_ActiveTrueOrderByUser_NameAsc(Long storeId);

    List<EmployeeEntity>
    findByStoreIsNullAndUser_ActiveTrueOrderByUser_NameAsc();

    Optional<EmployeeEntity> findByIdAndStore_Id(
            Long employeeId,
            Long storeId
    );

    @Query(value = """
            SELECT f.*
            FROM employees f
            LEFT JOIN appointment a ON f.id_user = a.employee_id
            GROUP BY f.id_user
            ORDER BY COUNT(a.employee_id) ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<EmployeeEntity> lessBusyEmployee();
}