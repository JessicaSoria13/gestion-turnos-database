package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.MedicoRequestDTO;
import com.turnosmedicos.backend.dto.MedicoResponseDTO;
import com.turnosmedicos.backend.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public List<MedicoResponseDTO> listar() {
        return medicoService.listarActivos();
    }

    @GetMapping("/{id}")
    public MedicoResponseDTO obtenerPorId(@PathVariable Integer id) {
        return medicoService.obtenerPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MedicoResponseDTO> crear(@Valid @RequestBody MedicoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public MedicoResponseDTO actualizar(@PathVariable Integer id, @Valid @RequestBody MedicoRequestDTO request) {
        return medicoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        medicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/horarios-disponibles")
    public List<String> obtenerHorariosDisponibles(
            @PathVariable Integer id,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return medicoService.obtenerHorariosDisponibles(id, fecha);
    }
}
