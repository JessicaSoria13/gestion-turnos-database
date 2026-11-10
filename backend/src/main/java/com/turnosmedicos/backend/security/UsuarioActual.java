package com.turnosmedicos.backend.security;

import com.turnosmedicos.backend.entity.Paciente;
import com.turnosmedicos.backend.entity.Usuario;
import com.turnosmedicos.backend.enums.Rol;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Datos del usuario autenticado (salen del JWT) y controles de propiedad.
//
// @PreAuthorize solo controla el ROL. Esto controla que un PACIENTE opere
// unicamente sobre SUS datos: sus turnos, sus coberturas, su ficha y su
// cuenta (regla 11: cada endpoint se restringe por rol y por usuario).
// El ADMINISTRADOR (medico/secretaria) gestiona a todos los pacientes.
//
// Si no corresponde, lanza AccessDeniedException -> 403 "Sin permisos"
// (GlobalExceptionHandler).
@Component
public class UsuarioActual {

    private static final String MENSAJE_PACIENTE = "No podés acceder a datos de otro paciente";
    private static final String MENSAJE_USUARIO = "No podés acceder a la cuenta de otro usuario";

    // DNI del usuario autenticado (el "username" del token).
    public String dni() {
        return autenticacion().getName();
    }

    // Rol con el que ingresó (un mismo DNI puede tener los dos roles,
    // pero cada token es de uno solo).
    public Rol rol() {
        return esAdministrador() ? Rol.ADMINISTRADOR : Rol.PACIENTE;
    }

    public boolean esAdministrador() {
        return autenticacion().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.ADMINISTRADOR.name()));
    }

    // El ADMINISTRADOR pasa siempre; un PACIENTE solo si es su propio paciente.
    public void verificarPaciente(Paciente paciente) {
        if (esAdministrador()) {
            return;
        }
        if (!paciente.getDni().equals(dni())) {
            throw new AccessDeniedException(MENSAJE_PACIENTE);
        }
    }

    // El ADMINISTRADOR pasa siempre; un PACIENTE solo sobre su propia cuenta
    // (misma fila de Usuario: mismo DNI y rol PACIENTE).
    public void verificarUsuario(Usuario usuario) {
        if (esAdministrador()) {
            return;
        }
        if (!usuario.getDni().equals(dni()) || usuario.getRol() != Rol.PACIENTE) {
            throw new AccessDeniedException(MENSAJE_USUARIO);
        }
    }

    // Para operaciones que solo puede hacer el ADMINISTRADOR dentro de un
    // endpoint que también usa el PACIENTE (ej: listar todas las coberturas).
    public void verificarAdministrador(String mensaje) {
        if (!esAdministrador()) {
            throw new AccessDeniedException(mensaje);
        }
    }

    private Authentication autenticacion() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
