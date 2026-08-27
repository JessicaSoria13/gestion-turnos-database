package com.turnosmedicos.backend.dto;

import com.turnosmedicos.backend.enums.Rol;

public record UsuarioResponseDTO(
        Integer id,
        String dni,
        Rol rol,
        Boolean activo
) {
}
