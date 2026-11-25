package com.github.banhosup.repository;

import com.github.banhosup.model.entities.EmployeePermissionEntity;
import com.github.banhosup.model.enums.EmployeePermission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeePermissionRepository
        extends JpaRepository<EmployeePermissionEntity, Long> {

    List<EmployeePermissionEntity>
    findAllByEmployee_IdOrderByPermissionAsc(Long employeeId);

    boolean existsByEmployee_User_EmailAndPermission(
            String email,
            EmployeePermission permission
    );

    void deleteAllByEmployee_Id(Long employeeId);

}