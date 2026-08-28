package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioContrasenaRequestDTO(

        @NotBlank
        String contrasenaActual,

        @NotBlank
        @Size(min = 6, max = 100)
        String contrasenaNueva

) {
}
