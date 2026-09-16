package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.ConsultaMedicaRequestDTO;
import com.turnosmedicos.backend.dto.ConsultaMedicaResponseDTO;
import com.turnosmedicos.backend.service.ConsultaMedicaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consultas-medicas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ConsultaMedicaController {

    private final ConsultaMedicaService consultaMedicaService;

    public ConsultaMedicaController(ConsultaMedicaService consultaMedicaService) {
        this.consultaMedicaService = consultaMedicaService;
    }

    @GetMapping
    public List<ConsultaMedicaResponseDTO> listar() {
        return consultaMedicaService.listar();
    }

    @GetMapping("/{id}")
    public ConsultaMedicaResponseDTO obtener(@PathVariable Integer id) {
        return consultaMedicaService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<ConsultaMedicaResponseDTO> crear(@Valid @RequestBody ConsultaMedicaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaMedicaService.crear(request));
    }
}
