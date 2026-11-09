package com.github.banhosup.dto;

import com.github.banhosup.model.entities.UserEntity;
import com.github.banhosup.model.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        Role role,
        boolean active,
        LocalDateTime registeredAt
) {

    public static UserResponse from(UserEntity userEntity) {
        return new UserResponse(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getPhone(),
                userEntity.getRole(),
                userEntity.isActive(),
                userEntity.getRegisteredAt()
        );
    }
}