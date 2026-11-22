package com.github.banhosup.controller;

import com.github.banhosup.dto.EmployeeRegisterRequest;
import com.github.banhosup.dto.UserRegisterRequest;
import com.github.banhosup.dto.UserResponse;
import com.github.banhosup.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }
}
