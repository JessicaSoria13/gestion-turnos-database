package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.CambioContrasenaRequestDTO;
import com.turnosmedicos.backend.dto.UsuarioRequestDTO;
import com.turnosmedicos.backend.dto.UsuarioResponseDTO;
import com.turnosmedicos.backend.entity.Usuario;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioResponseDTO> listarActivos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UsuarioResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        usuarioRepository.findByDni(request.dni()).ifPresent(u -> {
            throw new ConflictoException("Ya existe un usuario con el DNI '" + request.dni() + "'");
        });

        Usuario usuario = Usuario.builder()
                .dni(request.dni())
                .contrasena(passwordEncoder.encode(request.contrasena()))
                .rol(request.rol())
                .build();

        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO cambiarContrasena(Integer id, CambioContrasenaRequestDTO request) {
        Usuario usuario = buscarPorId(id);

        if (!passwordEncoder.matches(request.contrasenaActual(), usuario.getContrasena())) {
            throw new BadCredentialsException("La contraseña actual no es correcta");
        }

        usuario.setContrasena(passwordEncoder.encode(request.contrasenaNueva()));

        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public void eliminar(Integer id) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getDni(),
                usuario.getRol(),
                usuario.getActivo()
        );
    }

}
