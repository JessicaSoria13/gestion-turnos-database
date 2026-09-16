package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank
        String dni,

        @NotBlank
        String contrasena
) {
}
