package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.BloqueoRequestDTO;
import com.turnosmedicos.backend.dto.BloqueoResponseDTO;
import com.turnosmedicos.backend.service.BloqueoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bloqueos")
public class BloqueoController {

    private final BloqueoService bloqueoService;

    public BloqueoController(BloqueoService bloqueoService) {
        this.bloqueoService = bloqueoService;
    }

    @GetMapping
    public List<BloqueoResponseDTO> listar(@RequestParam(required = false) Integer idMedico) {
        return idMedico != null
                ? bloqueoService.listarPorMedico(idMedico)
                : bloqueoService.listarActivos();
    }

    @GetMapping("/{id}")
    public BloqueoResponseDTO obtener(@PathVariable Integer id) {
        return bloqueoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<BloqueoResponseDTO> crear(@Valid @RequestBody BloqueoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloqueoService.crear(request));
    }

    @PutMapping("/{id}")
    public BloqueoResponseDTO actualizar(@PathVariable Integer id, @Valid @RequestBody BloqueoRequestDTO request) {
        return bloqueoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        bloqueoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
