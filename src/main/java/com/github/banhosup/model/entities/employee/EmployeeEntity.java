package com.github.banhosup.model.entities.employee;

import com.github.banhosup.model.entities.store.StoreEntity;
import com.github.banhosup.model.entities.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
public class EmployeeEntity {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_user")
    private UserEntity user;

    private String registry;
    private String position;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private StoreEntity store;

    @Column(name = "team_active", nullable = false)
    private boolean teamActive = true;
}