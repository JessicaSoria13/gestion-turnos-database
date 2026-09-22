package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiagnosticoRequestDTO(

        @NotNull
        Integer consultaId,

        @NotBlank
        String descripcion

) {
}
