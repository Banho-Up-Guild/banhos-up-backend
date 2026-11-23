package com.github.banhosup.controller;

import com.github.banhosup.dto.StoreRequest;
import com.github.banhosup.dto.StoreResponse;
import com.github.banhosup.service.StoreService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lojas")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService lojaService;

    // LISTAR LOJAS
    @GetMapping
    public ResponseEntity<List<StoreResponse>> listar(
            @RequestParam(defaultValue = "true") boolean somenteAtivas) {

        return ResponseEntity.ok(
                lojaService.listar(somenteAtivas)
        );
    }

    // BUSCAR LOJA PELO ID
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                lojaService.buscarPorId(id)
        );
    }

    // CADASTRAR LOJA
    @PostMapping
    public ResponseEntity<StoreResponse> cadastrar(
            @RequestBody @Valid StoreRequest request) {

        StoreResponse response = lojaService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // ATUALIZAR LOJA
    @PutMapping("/{id}")
    public ResponseEntity<StoreResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid StoreRequest request) {

        return ResponseEntity.ok(
                lojaService.atualizar(id, request)
        );
    }

    // ATIVAR OU DESATIVAR LOJA
    @PatchMapping("/{id}/status")
    public ResponseEntity<StoreResponse> alterarStatus(
            @PathVariable Long id,
            @RequestParam boolean ativa) {

        return ResponseEntity.ok(
                lojaService.alterarStatus(id, ativa)
        );
    }
}