package com.turnosmedicos.backend.dto;

import com.turnosmedicos.backend.enums.DiaSemana;

import java.time.LocalTime;

public record DisponibilidadResponseDTO(
        Integer id,
        Integer idMedico,
        DiaSemana diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer duracionTurnoMin,
        Boolean activo
) {
}
