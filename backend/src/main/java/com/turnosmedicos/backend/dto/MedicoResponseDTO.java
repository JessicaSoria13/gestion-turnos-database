package com.turnosmedicos.backend.dto;

import java.util.List;

public record MedicoResponseDTO(
        Integer id,
        String nombre,
        String matricula,
        List<String> especialidades,
        Boolean activo
) {
}
