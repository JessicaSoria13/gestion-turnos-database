package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.NotNull;

public record ConsultaMedicaRequestDTO(
    @NotNull
    Integer idTurno,

    String observaciones
    ) {
}
