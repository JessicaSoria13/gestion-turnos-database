package com.turnosmedicos.backend.dto;

import com.turnosmedicos.backend.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(

        @NotBlank
        @Size(max = 10)
        String dni,

        @NotBlank
        @Size(min = 6, max = 100)
        String contrasena,

        @NotNull
        Rol rol

) {
}
