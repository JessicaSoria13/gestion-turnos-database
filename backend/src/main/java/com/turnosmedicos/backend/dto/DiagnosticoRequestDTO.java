package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiagnosticoRequestDTO(

        @NotNull
        Integer idConsulta,

        @NotBlank
        String descripcion

) {
}
