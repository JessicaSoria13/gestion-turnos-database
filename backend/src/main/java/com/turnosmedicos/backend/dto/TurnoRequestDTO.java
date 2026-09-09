package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;


public record TurnoRequestDTO(
        @NotNull
        Integer idPaciente,

        @NotNull
        Integer idMedico,

        @NotNull
        Integer idDisponibilidad,

        Integer idCobertura,

        @NotNull
        @FutureOrPresent
        LocalDate fecha,

        @NotNull
        LocalTime hora,

        @Size(max = 255)
        String motivoConsulta
) {
}
