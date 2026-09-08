package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteRequestDTO(

        @NotNull
        Integer idUsuario,

        @NotBlank
        @Size(max = 100)
        String nombreApellido,

        @NotBlank
        @Size(max = 10)
        String dni,

        @NotNull
        @Past
        LocalDate fechaNacimiento,

        @Email
        @Size(max = 100)
        String email,

        @Size(max = 20)
        String celular,

        @Size(max = 150)
        String domicilio

) {
}