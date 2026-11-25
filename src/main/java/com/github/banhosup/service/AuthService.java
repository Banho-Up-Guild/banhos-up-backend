package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.login.LoginRequest;
import com.github.banhosup.dto.login.LoginResponse;
import com.github.banhosup.dto.user.UserResponse;
import com.github.banhosup.model.entities.UserEntity;
import com.github.banhosup.repository.UserRepository;
import com.github.banhosup.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();

        UserEntity user;

        if (identifier.contains("@")) {

            String email = identifier.toLowerCase();

            user = userRepository.findByEmail(email)
                    .orElseThrow(this::invalidCredentials);

        } else {

            String cpf = onlyDigits(identifier);

            if (!cpf.matches("\\d{11}")) {
                throw invalidCredentials();
            }

            user = userRepository.findByCpf(cpf)
                    .orElseThrow(this::invalidCredentials);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw invalidCredentials();
        }

        if (!user.isActive()) {
            throw invalidCredentials();
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                UserResponse.from(user)
        );
    }


    private String onlyDigits(String value) {
        return value.replaceAll("\\D", "");
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(HttpStatus.UNAUTHORIZED, "Usuario ou senha inválidos.");
    }
}