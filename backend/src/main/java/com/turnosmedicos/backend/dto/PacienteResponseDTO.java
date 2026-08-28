package com.turnosmedicos.backend.dto;

import java.time.LocalDate;

public record PacienteResponseDTO(
        Integer id,
        Integer idUsuario,
        String nombreApellido,
        String dni,
        LocalDate fechaNacimiento,
        String email,
        String celular,
        String domicilio,
        Boolean activo
) {
}