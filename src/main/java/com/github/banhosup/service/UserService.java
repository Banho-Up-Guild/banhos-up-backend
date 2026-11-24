package com.github.banhosup.service;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.employee.EmployeeRegisterRequest;
import com.github.banhosup.dto.user.UserRegisterRequest;
import com.github.banhosup.dto.user.UserResponse;
import com.github.banhosup.model.entities.CustomerEntity;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.model.entities.UserEntity;
import com.github.banhosup.model.enums.Role;
import com.github.banhosup.repository.CustomerRepository;
import com.github.banhosup.repository.EmployeeRepository;
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
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

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
                Role.CUSTOMER
        );

        UserEntity saved = userRepository.save(user);
        customerRepository.save(new CustomerEntity(saved));
        return UserResponse.from(saved);
    }
    @Transactional
    public UserResponse registerEmployee(EmployeeRegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String cpf = onlyDigits(request.cpf());
        String phone = onlyDigits(request.phone());

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
        }
        if (userRepository.existsByCpf(cpf)) {
            throw new BusinessException(HttpStatus.CONFLICT, "CPF já cadastrado.");
        }
        if (employeeRepository.existsByRegistry(request.registry())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Matrícula já cadastrada.");
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

        EmployeeEntity employee = new EmployeeEntity();
        employee.setUser(saved);
        employee.setRegistry(request.registry());
        employee.setPosition(request.position());
        employeeRepository.save(employee);

        return UserResponse.from(saved);
    }

    public UserResponse findByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        return UserResponse.from(user);
    }

    private String onlyDigits(String value) {
        return value.replaceAll("\\D", "");
    }
}

