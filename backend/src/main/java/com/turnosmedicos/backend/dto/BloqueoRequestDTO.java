package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BloqueoRequestDTO(

        @NotNull
        Integer idMedico,

        @NotNull
        LocalDate fechaDesde,

        @NotNull
        LocalDate fechaHasta,

        @NotBlank
        @Size(max = 100)
        String motivo

) {
}
