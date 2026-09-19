package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.TratamientoRequestDTO;
import com.turnosmedicos.backend.dto.TratamientoResponseDTO;
import com.turnosmedicos.backend.service.TratamientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/tratamientos")
public class TratamientoController {

    private final TratamientoService tratamientoService;

    public TratamientoController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    @GetMapping
    public List<TratamientoResponseDTO> listar(@RequestParam(required = false) Integer idDiagnostico) {
        return tratamientoService.listar(idDiagnostico);
    }

    @GetMapping("/{id}")
    public TratamientoResponseDTO obtener(@PathVariable Integer id) {
        return tratamientoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<TratamientoResponseDTO> crear(@Valid @RequestBody TratamientoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tratamientoService.crear(request));
    }

    @PutMapping("/{id}")
    public TratamientoResponseDTO actualizar(@PathVariable Integer id,
                                             @Valid @RequestBody TratamientoRequestDTO request) {
        return tratamientoService.actualizar(id, request);
    }

}
