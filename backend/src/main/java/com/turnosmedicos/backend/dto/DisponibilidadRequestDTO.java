package com.turnosmedicos.backend.dto;

import com.turnosmedicos.backend.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalTime;

public record DisponibilidadRequestDTO(

        @NotNull
        Integer idMedico,

        @NotNull
        DiaSemana diaSemana,

        @NotNull
        LocalTime horaInicio,

        @NotNull
        LocalTime horaFin,

        @NotNull
        @Positive
        Integer duracionTurnoMin

) {
}
