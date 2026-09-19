package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TratamientoRequestDTO(

        @NotNull
        Integer idDiagnostico,

        @NotBlank
        String descripcion,

        @Size(max = 50)
        String duracion

) {
}
