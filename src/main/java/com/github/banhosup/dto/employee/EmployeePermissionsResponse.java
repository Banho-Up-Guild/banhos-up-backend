package com.github.banhosup.dto.employee;

import com.github.banhosup.model.enums.EmployeePermission;

import java.util.List;

public record EmployeePermissionsResponse(

        Long employeeId,

        String employeeName,

        List<EmployeePermission> permissions

) {
}