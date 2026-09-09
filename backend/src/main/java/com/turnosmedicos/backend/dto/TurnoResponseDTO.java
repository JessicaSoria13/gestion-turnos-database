package com.turnosmedicos.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import com.turnosmedicos.backend.enums.EstadoTurno;


public record TurnoResponseDTO (
        Integer id,
        Integer idPaciente,
        Integer idMedico,
        Integer idDisponibilidad,
        Integer idCobertura,
        LocalDate fecha,
        LocalTime hora,
        EstadoTurno estado,
        String motivoConsulta,
        String motivoCancelacion
)
{
}
