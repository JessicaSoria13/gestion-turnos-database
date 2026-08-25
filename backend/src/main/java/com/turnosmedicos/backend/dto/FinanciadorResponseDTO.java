package com.turnosmedicos.backend.dto;

public record FinanciadorResponseDTO(
        Integer id,
        String nombre,
        String cuit,
        Boolean activo
) {
}
