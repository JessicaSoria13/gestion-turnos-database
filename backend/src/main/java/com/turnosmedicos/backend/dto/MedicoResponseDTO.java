package com.turnosmedicos.backend.dto;

public record MedicoResponseDTO(
        Integer id,
        String nombre,
        String matricula,
        String especialidad,
        Boolean activo
) {
}
