package com.github.banhosup.model.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id_user")
    private UserEntity user;

    @Setter
    private String address;

    @Setter
    @Column(length = 30)
    private String gender;

    @Setter
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    public CustomerEntity(UserEntity user) {
        this.user = user;
    }
}