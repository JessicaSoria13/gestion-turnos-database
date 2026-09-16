package com.turnosmedicos.backend.dto;

import com.turnosmedicos.backend.enums.Rol;

public record LoginResponseDTO(
        String token,
        Rol rol
) {
}
