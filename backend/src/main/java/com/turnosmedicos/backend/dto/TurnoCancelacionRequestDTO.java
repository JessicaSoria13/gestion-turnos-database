package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.Size;

public record TurnoCancelacionRequestDTO(
        boolean canceladoPorMedico,

        @Size(max = 255)
        String motivoCancelacion
){
}
