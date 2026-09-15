package com.turnosmedicos.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ConsultaMedicaResponseDTO(
        Integer id,
        Integer idTurno,
        LocalDate fecha,
        LocalTime hora,
        String observaciones

)
{
}
