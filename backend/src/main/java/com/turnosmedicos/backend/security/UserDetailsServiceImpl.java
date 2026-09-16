package com.turnosmedicos.backend.security;

import com.turnosmedicos.backend.entity.Usuario;
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

    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByDni(dni)
                .filter(Usuario::getActivo)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario activo con DNI: " + dni));

        return User.builder()
                .username(usuario.getDni())
                .password(usuario.getContrasena())
                .roles(usuario.getRol().name())
                .build();
    }
}
