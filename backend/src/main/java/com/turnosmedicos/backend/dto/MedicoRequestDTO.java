package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MedicoRequestDTO(

        @NotNull
        Integer idUsuario,

        @NotBlank
        @Size(max = 100)
        String nombre,

        @NotBlank
        @Size(max = 10)
        String dni,

        @NotBlank
        @Size(max = 20)
        String matricula,

        List<String> especialidades

) {
}
