package com.github.banhosup.controller.employee;

import com.github.banhosup.dto.employee.ProfessionalResponse;
import com.github.banhosup.repository.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PublicEmployeeController {

    private final EmployeeRepository employeeRepository;

    @GetMapping("/store_employee")
    public ResponseEntity<List<ProfessionalResponse>> listarFuncionarios(
            @RequestParam Long idStore
    ) {

        List<ProfessionalResponse> profissionais =
                employeeRepository
                        .findByStore_IdAndTeamActiveTrueAndUser_ActiveTrueOrderByUser_NameAsc(idStore)
                        .stream()
                        .map(employee -> new ProfessionalResponse(
                                employee.getId(),
                                employee.getUser().getName()
                        ))
                        .toList();

        return ResponseEntity.ok(profissionais);
    }
}