package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.TurnoRequestDTO;
import com.turnosmedicos.backend.dto.TurnoResponseDTO;
import com.turnosmedicos.backend.service.TurnoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.turnosmedicos.backend.dto.TurnoCancelacionRequestDTO;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@RestController
@RequestMapping("/api/v1/turnos")
public class TurnoController {

    private final TurnoService turnoService;

    public TurnoController(TurnoService turnoService) {
        this.turnoService = turnoService;
    }

    @GetMapping
    public List<TurnoResponseDTO> listar() {
        return turnoService.listar();
    }

    @GetMapping("/{id}")
    public TurnoResponseDTO obtener(@PathVariable Integer id) {
        return turnoService.obtenerPorId(id);
    }

    @PutMapping("/{id}/cancelar")
    public TurnoResponseDTO cancelar(@PathVariable Integer id, @Valid @RequestBody TurnoCancelacionRequestDTO request) {
        return turnoService.cancelar(id, request);
    }


    @PostMapping
    public ResponseEntity<TurnoResponseDTO> crear(@Valid @RequestBody TurnoRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crear(request));
    }
}
