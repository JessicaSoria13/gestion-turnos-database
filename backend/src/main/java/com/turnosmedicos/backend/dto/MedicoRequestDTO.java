package com.turnosmedicos.backend.dto;

public record MedicoRequestDTO(
        String nombre,
        String matricula,
        String especialidad
) {
}
