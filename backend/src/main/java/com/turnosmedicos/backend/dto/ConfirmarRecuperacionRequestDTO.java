package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmarRecuperacionRequestDTO(
        @NotBlank
        String token,

        @NotBlank
        @Size(min = 6, max = 100)
        String contrasenaNueva
) {
}
