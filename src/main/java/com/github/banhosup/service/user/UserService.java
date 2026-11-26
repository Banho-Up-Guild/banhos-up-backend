package com.github.banhosup.service.user;

import com.github.banhosup.common.exceptions.BusinessException;
import com.github.banhosup.dto.password.ChangePasswordRequest;
import com.github.banhosup.dto.employee.EmployeeRegisterRequest;
import com.github.banhosup.dto.user.UserRegisterRequest;
import com.github.banhosup.dto.user.UserResponse;
import com.github.banhosup.dto.user.UserUpdateRequest;
import com.github.banhosup.model.entities.customer.CustomerEntity;
import com.github.banhosup.model.entities.employee.EmployeeEntity;
import com.github.banhosup.model.entities.store.StoreEntity;
import com.github.banhosup.model.entities.user.UserEntity;
import com.github.banhosup.model.enums.Role;
import com.github.banhosup.repository.customer.CustomerRepository;
import com.github.banhosup.repository.employee.EmployeeRepository;
import com.github.banhosup.repository.store.StoreRepository;
import com.github.banhosup.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.banhosup.dto.customer.CustomerProfileResponse;
import com.github.banhosup.dto.customer.CustomerProfileUpdateRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;

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

        user.setSecondaryPhone(normalizeOptionalPhone(request.secondaryPhone()));

        UserEntity saved = userRepository.save(user);

        CustomerEntity customer = new CustomerEntity(saved);

        customer.setGender(normalizeOptionalText(request.gender()));
        customer.setDateOfBirth(request.dateOfBirth());

        customerRepository.save(customer);

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

        StoreEntity store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "Loja não encontrada."
                ));

        if (!store.isAtiva()) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível cadastrar funcionários em uma unidade inativa."
            );
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
        employee.setStore(store);
        employeeRepository.save(employee);

        return UserResponse.from(saved);
    }

    public UserResponse findByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse findMyCustomerProfile(String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        if (user.getRole() != Role.CUSTOMER) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Esta operação é exclusiva para clientes."
            );
        }

        CustomerEntity customer = customerRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Perfil do cliente não encontrado."
                ));

        return CustomerProfileResponse.from(user, customer);
    }

    @Transactional
    public CustomerProfileResponse updateMyCustomerProfile(
            String email,
            CustomerProfileUpdateRequest request
    ) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        if (user.getRole() != Role.CUSTOMER || !user.isActive()) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Esta operação não está disponível para esta conta."
            );
        }

        CustomerEntity customer = customerRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Perfil do cliente não encontrado."
                ));

        user.setName(request.name().trim());
        user.setPhone(onlyDigits(request.phone()));
        user.setSecondaryPhone(
                normalizeOptionalPhone(request.secondaryPhone())
        );

        customer.setGender(normalizeOptionalText(request.gender()));
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setAddress(normalizeOptionalText(request.address()));

        userRepository.save(user);
        customerRepository.save(customer);

        return CustomerProfileResponse.from(user, customer);
    }

    @Transactional
    public void changeMyCustomerPassword(
            String email,
            ChangePasswordRequest request
    ) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        if (user.getRole() != Role.CUSTOMER || !user.isActive()) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN,
                    "Esta operação está disponível somente para clientes ativos."
            );
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A senha atual informada está incorreta."
            );
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "A nova senha deve ser diferente da senha atual."
            );
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public List<UserResponse> findAll() {
        List<UserResponse> users = new ArrayList<>();

        for (UserEntity user : userRepository.findAll()) {
            users.add(UserResponse.from(user));
        }

        return users;
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        UserEntity user = findById(id);
        user.setName(request.name().trim());
        user.setPhone(onlyDigits(request.phone()));
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active) {
        UserEntity user = findById(id);
        user.setActive(active);
        return UserResponse.from(userRepository.save(user));
    }

    private UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    private String onlyDigits(String value) {
        return value.replaceAll("\\D", "");
    }

    private String normalizeOptionalPhone(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return onlyDigits(value);
    }

    private String normalizeOptionalText(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}