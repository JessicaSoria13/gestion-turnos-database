package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.PacienteRequestDTO;
import com.turnosmedicos.backend.dto.PacienteResponseDTO;
import com.turnosmedicos.backend.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public List<PacienteResponseDTO> listar() {
        return pacienteService.listarActivos();
    }

    @GetMapping("/{id}")
    public PacienteResponseDTO obtener(@PathVariable Integer id) {
        return pacienteService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<PacienteResponseDTO> crear(@Valid @RequestBody PacienteRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crear(request));
    }

    @PutMapping("/{id}")
    public PacienteResponseDTO actualizar(@PathVariable Integer id, @Valid @RequestBody PacienteRequestDTO request) {
        return pacienteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}