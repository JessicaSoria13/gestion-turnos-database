package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.Size;

// Quién cancela (paciente o médico) no viaja en el body: lo decide el backend
// según el rol del token (ver TurnoService.cancelar). El motivo es opcional
// para el PACIENTE y obligatorio para el ADMINISTRADOR (regla 6).
public record TurnoCancelacionRequestDTO(
        @Size(max = 255)
        String motivoCancelacion
){
}
