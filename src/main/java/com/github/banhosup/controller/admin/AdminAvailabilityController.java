package com.github.banhosup.controller.admin;

import com.github.banhosup.dto.availability.StoreAvailabilityDayResponse;
import com.github.banhosup.dto.availability.StoreAvailabilityRequest;
import com.github.banhosup.service.appoiintment.AvailabilityService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/stores/{storeId}/availability")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<StoreAvailabilityDayResponse>> consultar(
            @PathVariable Long storeId
    ) {

        return ResponseEntity.ok(
                availabilityService.listarPorUnidade(storeId)
        );
    }

    @PutMapping
    public ResponseEntity<List<StoreAvailabilityDayResponse>> salvar(
            @PathVariable Long storeId,
            @RequestBody @Valid StoreAvailabilityRequest request
    ) {

        return ResponseEntity.ok(
                availabilityService.salvarPorUnidade(
                        storeId,
                        request
                )
        );
    }
}