package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CoberturaRequestDTO(

        @NotNull
        Integer idPaciente,

        @NotNull
        Integer idFinanciador,

        @NotBlank
        @Size(max = 30)
        String numeroAfiliado,

        @Size(max = 50)
        String plan

) {
}
