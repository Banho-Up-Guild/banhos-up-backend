package com.github.banhosup.model.entities;

import com.github.banhosup.model.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // só o JPA usa
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @Column(nullable = false)
    private String password;

    @Setter
    @Column(nullable = false, length = 20)
    private String phone;

    @Setter
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Setter
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    public User(String name, String email, String password, String phone, String cpf, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.cpf = cpf;
        this.role = role;
    }

    @PrePersist
    void onRegister() {
        registeredAt = LocalDateTime.now();
    }
}