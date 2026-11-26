package com.github.banhosup.model.entities.employee;

import com.github.banhosup.model.enums.EmployeePermission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "employee_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_permission",
                        columnNames = {"employee_id", "permission"}
                )
        }
)
@Getter
@NoArgsConstructor
public class EmployeePermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private EmployeeEntity employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EmployeePermission permission;

    public EmployeePermissionEntity(
            EmployeeEntity employee,
            EmployeePermission permission
    ) {
        this.employee = employee;
        this.permission = permission;
    }
}