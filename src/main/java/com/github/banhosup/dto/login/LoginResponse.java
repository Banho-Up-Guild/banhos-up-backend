package com.github.banhosup.dto.login;

import com.github.banhosup.dto.user.UserResponse;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserResponse user
) {
}