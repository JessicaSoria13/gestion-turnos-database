package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.MedicoRequestDTO;
import com.turnosmedicos.backend.dto.MedicoResponseDTO;
import com.turnosmedicos.backend.service.MedicoService;
import org.springframework.format.annotation.DateTimeFormat;
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

    @GetMapping("/{id}")
    public MedicoResponseDTO obtenerPorId(@PathVariable Integer id) {
        return medicoService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MedicoResponseDTO actualizar(@PathVariable Integer id, @RequestBody MedicoRequestDTO request) {
        return medicoService.actualizar(id, request);
    }

    @GetMapping("/{id}/horarios-disponibles")
    public List<String> obtenerHorariosDisponibles(
            @PathVariable Integer id,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return medicoService.obtenerHorariosDisponibles(id, fecha);
    }
}
