package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FinanciadorRequestDTO(

        @NotBlank
        @Size(max = 100)
        String nombre,

        @Size(max = 13)
        String cuit

) {
}
