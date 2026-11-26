package com.github.banhosup.dto;

import com.github.banhosup.model.entities.CustomerEntity;
import com.github.banhosup.model.entities.UserEntity;
import com.github.banhosup.model.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerProfileResponse(

        Long id,
        String name,
        String email,
        String phone,
        String secondaryPhone,
        String cpf,
        String gender,
        LocalDate dateOfBirth,
        String address,
        Role role,
        boolean active,
        LocalDateTime registeredAt

) {

    public static CustomerProfileResponse from(
            UserEntity user,
            CustomerEntity customer
    ) {

        return new CustomerProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getSecondaryPhone(),
                user.getCpf(),
                customer.getGender(),
                customer.getDateOfBirth(),
                customer.getAddress(),
                user.getRole(),
                user.isActive(),
                user.getRegisteredAt()
        );
    }
}