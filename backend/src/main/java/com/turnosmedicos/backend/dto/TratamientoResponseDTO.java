package com.turnosmedicos.backend.dto;

import com.turnosmedicos.backend.enums.EstadoTratamiento;

public record TratamientoResponseDTO(
        Integer id,
        Integer idDiagnostico,
        String descripcion,
        String duracion,
        EstadoTratamiento estado
) {
}
