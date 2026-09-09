package com.turnosmedicos.backend.dto;

public record CoberturaResponseDTO(
        Integer id,
        Integer idPaciente,
        Integer idFinanciador,
        String numeroAfiliado,
        String plan,
        Boolean activo
) {
}
