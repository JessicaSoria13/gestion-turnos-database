package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.DiagnosticoRequestDTO;
import com.turnosmedicos.backend.dto.DiagnosticoResponseDTO;
import com.turnosmedicos.backend.service.DiagnosticoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/diagnosticos")
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    public DiagnosticoController(DiagnosticoService diagnosticoService) {
        this.diagnosticoService = diagnosticoService;
    }

    @GetMapping
    public List<DiagnosticoResponseDTO> listar() {
        return diagnosticoService.listar();
    }

    @GetMapping("/{id}")
    public DiagnosticoResponseDTO obtener(@PathVariable Integer id) {
        return diagnosticoService.obtenerPorId(id);
    }

    @GetMapping("/consulta/{consultaId}")
    public List<DiagnosticoResponseDTO> listarPorConsulta(@PathVariable Integer consultaId) {
        return diagnosticoService.listarPorConsulta(consultaId);
    }

    @PostMapping
    public ResponseEntity<DiagnosticoResponseDTO> crear(
            @Valid @RequestBody DiagnosticoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(diagnosticoService.crear(request));
    }

    @PutMapping("/{id}")
    public DiagnosticoResponseDTO actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody DiagnosticoRequestDTO request) {
        return diagnosticoService.actualizar(id, request);
    }

}
