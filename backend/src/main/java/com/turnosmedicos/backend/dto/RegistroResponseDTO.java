package com.turnosmedicos.backend.dto;

public record RegistroResponseDTO(
        Integer idUsuario,
        Integer idPaciente,
        String mensaje
) {
}
