package com.github.banhosup.controller;

import com.github.banhosup.dto.employee.request.EmployeeRegisterRequest;
import com.github.banhosup.dto.employee.request.EmployeeStoreRequest;
import com.github.banhosup.dto.employee.response.EmployeeStoreResponse;
import com.github.banhosup.dto.user.UserRegisterRequest;
import com.github.banhosup.dto.user.UserResponse;
import com.github.banhosup.dto.UserUpdateRequest;
import com.github.banhosup.model.entities.EmployeeEntity;
import com.github.banhosup.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid UserRegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/staff")
    public ResponseEntity<UserResponse> registerEmployee(@RequestBody @Valid EmployeeRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerEmployee(request));
    }
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }
    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public UserResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return userService.setActive(id, active);
    }

    @GetMapping("/store_employee")
    public ResponseEntity<List<EmployeeStoreResponse>> getEmployeeStore (EmployeeStoreRequest dto){
        List<EmployeeEntity> employees = userService.findEmployeeStore(dto);

        List<EmployeeStoreResponse> response = employees.stream()
                .map(entity -> new EmployeeStoreResponse(
                        entity.getId(),
                        entity.getUser().getName()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}