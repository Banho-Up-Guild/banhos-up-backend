package com.github.banhosup.controller.admin;

import com.github.banhosup.dto.store.StoreResponse;
import com.github.banhosup.service.store.StoreService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/lojas")
@RequiredArgsConstructor
public class AdminStoreController {

    private final StoreService storeService;

    // ADMIN consulta todas as unidades, inclusive inativas.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StoreResponse>> listarTodas() {

        return ResponseEntity.ok(
                storeService.listar(false)
        );
    }
}