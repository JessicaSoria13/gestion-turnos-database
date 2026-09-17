package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.FinanciadorRequestDTO;
import com.turnosmedicos.backend.dto.FinanciadorResponseDTO;
import com.turnosmedicos.backend.service.FinanciadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/financiadores")
public class FinanciadorController {

    private final FinanciadorService financiadorService;

    public FinanciadorController(FinanciadorService financiadorService) {
        this.financiadorService = financiadorService;
    }

    @GetMapping
    public List<FinanciadorResponseDTO> listar() {
        return financiadorService.listarActivos();
    }

    @GetMapping("/{id}")
    public FinanciadorResponseDTO obtener(@PathVariable Integer id) {
        return financiadorService.obtenerPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<FinanciadorResponseDTO> crear(@Valid @RequestBody FinanciadorRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financiadorService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public FinanciadorResponseDTO actualizar(@PathVariable Integer id, @Valid @RequestBody FinanciadorRequestDTO request) {
        return financiadorService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        financiadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
