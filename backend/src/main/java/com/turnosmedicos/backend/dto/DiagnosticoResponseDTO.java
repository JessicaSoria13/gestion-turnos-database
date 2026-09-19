package com.turnosmedicos.backend.dto;

public record DiagnosticoResponseDTO(
        Integer id,
        Integer consultaId,
        String descripcion
) {
}
