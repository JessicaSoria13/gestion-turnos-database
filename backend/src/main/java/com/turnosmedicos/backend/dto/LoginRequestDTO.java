package com.turnosmedicos.backend.dto;

import com.turnosmedicos.backend.enums.Rol;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank
        String dni,

        @NotBlank
        String contrasena,

        // Opcional: solo hace falta si el dni tiene un Usuario PACIENTE y otro
        // ADMINISTRADOR activos a la vez, para indicar con cual entrar.
        Rol rol
) {
}
