package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.CoberturaRequestDTO;
import com.turnosmedicos.backend.dto.CoberturaResponseDTO;
import com.turnosmedicos.backend.service.CoberturaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coberturas")
public class CoberturaController {

    private final CoberturaService coberturaService;

    public CoberturaController(CoberturaService coberturaService) {
        this.coberturaService = coberturaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<CoberturaResponseDTO> listar(@RequestParam(required = false) Integer idPaciente) {
        return idPaciente != null
                ? coberturaService.listarPorPaciente(idPaciente)
                : coberturaService.listarActivos();
    }

    @GetMapping("/{id}")
    public CoberturaResponseDTO obtener(@PathVariable Integer id) {
        return coberturaService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<CoberturaResponseDTO> crear(@Valid @RequestBody CoberturaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(coberturaService.crear(request));
    }

    @PutMapping("/{id}")
    public CoberturaResponseDTO actualizar(@PathVariable Integer id, @Valid @RequestBody CoberturaRequestDTO request) {
        return coberturaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        coberturaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
