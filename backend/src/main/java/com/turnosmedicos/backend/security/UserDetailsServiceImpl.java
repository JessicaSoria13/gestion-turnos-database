package com.turnosmedicos.backend.security;

import com.turnosmedicos.backend.entity.Usuario;
import com.turnosmedicos.backend.enums.Rol;
import com.turnosmedicos.backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Usado por el filtro JWT: el token ya trae el rol, asi que carga
    // exactamente esa fila de Usuario y no cualquiera con ese dni (un dni
    // puede tener una fila PACIENTE y otra ADMINISTRADOR a la vez).
    public UserDetails loadUserByDniYRol(String dni, String rol) {
        Usuario usuario = usuarioRepository.findByDniAndRol(dni, Rol.valueOf(rol))
                .filter(Usuario::getActivo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe un usuario activo con DNI " + dni + " y rol " + rol));

        return toUserDetails(usuario);
    }

    // Requerido por la interfaz UserDetailsService. No lo usa el filtro JWT
    // (que ya conoce el rol via loadUserByDniYRol); si el dni tiene mas de
    // un rol activo, se queda con el primero que encuentre.
    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByDni(dni).stream()
                .filter(Usuario::getActivo)
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario activo con DNI: " + dni));

        return toUserDetails(usuario);
    }

    private UserDetails toUserDetails(Usuario usuario) {
        return User.builder()
                .username(usuario.getDni())
                .password(usuario.getContrasena())
                .roles(usuario.getRol().name())
                .build();
    }
}
