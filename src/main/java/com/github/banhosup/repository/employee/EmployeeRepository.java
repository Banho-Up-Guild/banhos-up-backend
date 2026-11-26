package com.github.banhosup.repository.employee;

import com.github.banhosup.model.entities.employee.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<EmployeeEntity>
    findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(
            Long storeId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EmployeeEntity e WHERE e.id = :employeeId")
    Optional<EmployeeEntity> findByIdForUpdate(
            @Param("employeeId") Long employeeId
    );

    List<EmployeeEntity>
    findByTeamActiveFalseAndUser_ActiveTrueOrderByUser_NameAsc();
}