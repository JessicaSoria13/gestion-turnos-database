package com.turnosmedicos.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegistroRequestDTO(
        @NotBlank
        @Size(max = 10)
        String dni,

        @NotBlank
        @Size(min = 6, max = 100)
        String contrasena,

        @NotBlank
        @Size(max = 100)
        String nombreApellido,

        @Email
        @Size(max = 100)
        String email,

        @Size(max = 20)
        String celular,

        @NotNull
        @Past
        LocalDate fechaNacimiento
) {
}
