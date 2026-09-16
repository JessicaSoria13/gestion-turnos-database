package com.turnosmedicos.backend.controller;

import com.turnosmedicos.backend.dto.ConfirmarRecuperacionRequestDTO;
import com.turnosmedicos.backend.dto.LoginRequestDTO;
import com.turnosmedicos.backend.dto.LoginResponseDTO;
import com.turnosmedicos.backend.dto.MensajeResponseDTO;
import com.turnosmedicos.backend.dto.RegistroRequestDTO;
import com.turnosmedicos.backend.dto.RegistroResponseDTO;
import com.turnosmedicos.backend.dto.SolicitarRecuperacionRequestDTO;
import com.turnosmedicos.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistroResponseDTO> register(@Valid @RequestBody RegistroRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/recuperar-contrasena/solicitar")
    public MensajeResponseDTO solicitarRecuperacion(@Valid @RequestBody SolicitarRecuperacionRequestDTO request) {
        return authService.solicitarRecuperacion(request);
    }

    @PostMapping("/recuperar-contrasena/confirmar")
    public MensajeResponseDTO confirmarRecuperacion(@Valid @RequestBody ConfirmarRecuperacionRequestDTO request) {
        return authService.confirmarRecuperacion(request);
    }
}
