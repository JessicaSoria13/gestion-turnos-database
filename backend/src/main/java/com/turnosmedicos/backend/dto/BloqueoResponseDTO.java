package com.turnosmedicos.backend.dto;

import java.time.LocalDate;

public record BloqueoResponseDTO(
        Integer id,
        Integer idMedico,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String motivo,
        Boolean activo
) {
}
