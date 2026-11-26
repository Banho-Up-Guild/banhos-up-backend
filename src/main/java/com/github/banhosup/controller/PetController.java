package com.github.banhosup.controller;

import com.github.banhosup.dto.PetDto;
import com.github.banhosup.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.List;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService service;

    // GET /pets -> pets do cliente autenticado
// O parâmetro donoId permanece por compatibilidade,
// mas não será utilizado para definir o proprietário.
    @GetMapping
    public List<PetDto> listar(
            @RequestParam(required = false) Long donoId,
            Authentication authentication
    ) {
        return service.listar(authentication.getName());
    }

    @GetMapping("/{id}")
    public PetDto buscarPorId(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return service.buscarPorId(id, authentication.getName());
    }

    @PostMapping
    public ResponseEntity<PetDto> criar(
            @Valid @RequestBody PetDto dto,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(dto, authentication.getName()));
    }

    @PutMapping("/{id}")
    public PetDto atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PetDto dto,
            Authentication authentication
    ) {
        return service.atualizar(id, dto, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        service.deletar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}