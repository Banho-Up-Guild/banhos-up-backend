package com.github.banhosup.controller.user;

import com.github.banhosup.dto.password.ChangePasswordRequest;
import com.github.banhosup.dto.employee.EmployeeRegisterRequest;
import com.github.banhosup.dto.user.UserRegisterRequest;
import com.github.banhosup.dto.user.UserResponse;
import com.github.banhosup.dto.user.UserUpdateRequest;
import com.github.banhosup.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.github.banhosup.dto.customer.CustomerProfileResponse;
import com.github.banhosup.dto.customer.CustomerProfileUpdateRequest;

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

    @GetMapping("/me/profile")
    public CustomerProfileResponse myProfile(
            Authentication authentication
    ) {
        return userService.findMyCustomerProfile(authentication.getName());
    }

    @PutMapping("/me/profile")
    public CustomerProfileResponse updateMyProfile(
            @RequestBody @Valid CustomerProfileUpdateRequest request,
            Authentication authentication
    ) {
        return userService.updateMyCustomerProfile(
                authentication.getName(),
                request
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @RequestBody @Valid ChangePasswordRequest request,
            Authentication authentication
    ) {
        userService.changeMyCustomerPassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @RequestBody @Valid UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @PatchMapping("/{id}/active")
    public UserResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return userService.setActive(id, active);
    }
}