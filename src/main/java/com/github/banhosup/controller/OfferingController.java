package com.github.banhosup.controller;

import com.github.banhosup.dto.offering.OfferingDto;
import com.github.banhosup.service.OfferingService;
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

import java.util.List;

@RestController
@RequestMapping("/Offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService service;

    // GET /Offerings             -> todos
    // GET /Offerings?ativos=true -> só os ativos (catálogo)
    @GetMapping
    public List<OfferingDto> listar(@RequestParam(defaultValue = "false") boolean ativos) {
        return service.listar(ativos);
    }

    @GetMapping("/{id}")
    public OfferingDto buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<OfferingDto> criar(@Valid @RequestBody OfferingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public OfferingDto atualizar(@PathVariable Long id, @Valid @RequestBody OfferingDto dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}