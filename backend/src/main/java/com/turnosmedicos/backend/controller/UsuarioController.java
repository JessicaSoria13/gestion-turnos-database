package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.CambioContrasenaRequestDTO;
import com.turnosmedicos.backend.dto.UsuarioRequestDTO;
import com.turnosmedicos.backend.dto.UsuarioResponseDTO;
import com.turnosmedicos.backend.service.UsuarioService;
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
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<UsuarioResponseDTO> listar() {
        return usuarioService.listarActivos();
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO obtener(@PathVariable Integer id) {
        return usuarioService.obtenerPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}/contrasena")
    public UsuarioResponseDTO cambiarContrasena(@PathVariable Integer id, @Valid @RequestBody CambioContrasenaRequestDTO request) {
        return usuarioService.cambiarContrasena(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
