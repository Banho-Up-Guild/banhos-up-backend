package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.UserRegisterRequest;
import com.github.banhosup.dto.UserResponse;
import com.github.banhosup.model.entities.UserEntity;
import com.github.banhosup.model.enums.Role;
import com.github.banhosup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String cpf = onlyDigits(request.cpf());
        String phone = onlyDigits(request.phone());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        if (userRepository.existsByCpf(cpf)) {
            throw new BusinessException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }

        UserEntity user = new UserEntity(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                phone,
                cpf,
                Role.EMPLOYEE
        );

        UserEntity saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    private String onlyDigits(String value) {
        return value.replaceAll("\\D", "");
    }
}