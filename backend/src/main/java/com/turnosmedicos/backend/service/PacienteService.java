package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.PacienteRequestDTO;
import com.turnosmedicos.backend.dto.PacienteResponseDTO;
import com.turnosmedicos.backend.entity.Paciente;
import com.turnosmedicos.backend.entity.Usuario;
import com.turnosmedicos.backend.enums.Rol;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.PacienteRepository;
import com.turnosmedicos.backend.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public PacienteService(PacienteRepository pacienteRepository, UsuarioRepository usuarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<PacienteResponseDTO> listarActivos() {
        return pacienteRepository.findByActivoTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PacienteResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    // El token solo trae el dni, no el id de paciente: lo resuelve a partir
    // del dni del usuario autenticado (igual que hace el filtro JWT).
    public PacienteResponseDTO obtenerPropio() {
        String dni = SecurityContextHolder.getContext().getAuthentication().getName();
        return pacienteRepository.findByDni(dni)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un paciente asociado al dni " + dni));
    }

    public PacienteResponseDTO crear(PacienteRequestDTO request) {
        Usuario usuario = buscarUsuario(request.idUsuario());

        if (usuario.getRol() != Rol.PACIENTE) {
            throw new ConflictoException("El usuario " + request.idUsuario() + " no tiene rol PACIENTE");
        }
        if (!usuario.getDni().equals(request.dni())) {
            throw new ConflictoException("El DNI del paciente debe coincidir con el DNI del usuario");
        }
        if (pacienteRepository.existsByUsuarioId(request.idUsuario())) {
            throw new ConflictoException("El usuario " + request.idUsuario() + " ya tiene un paciente asociado");
        }
        if (pacienteRepository.existsByDni(request.dni())) {
            throw new ConflictoException("Ya existe un paciente con el DNI '" + request.dni() + "'");
        }

        Paciente paciente = Paciente.builder()
                .usuario(usuario)
                .nombreApellido(request.nombreApellido())
                .dni(request.dni())
                .fechaNacimiento(request.fechaNacimiento())
                .email(request.email())
                .celular(request.celular())
                .domicilio(request.domicilio())
                .build();

        return toResponseDTO(pacienteRepository.save(paciente));
    }

    public PacienteResponseDTO actualizar(Integer id, PacienteRequestDTO request) {
        Paciente paciente = buscarPorId(id);

        paciente.setNombreApellido(request.nombreApellido());
        paciente.setEmail(request.email());
        paciente.setCelular(request.celular());
        paciente.setDomicilio(request.domicilio());

        return toResponseDTO(pacienteRepository.save(paciente));
    }

    public void eliminar(Integer id) {
        Paciente paciente = buscarPorId(id);
        paciente.setActivo(false);
        pacienteRepository.save(paciente);
    }

    private Paciente buscarPorId(Integer id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el paciente con id " + id));
    }

    private Usuario buscarUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + id));
    }

    private PacienteResponseDTO toResponseDTO(Paciente paciente) {
        return new PacienteResponseDTO(
                paciente.getId(),
                paciente.getUsuario().getId(),
                paciente.getNombreApellido(),
                paciente.getDni(),
                paciente.getFechaNacimiento(),
                paciente.getEmail(),
                paciente.getCelular(),
                paciente.getDomicilio(),
                paciente.getActivo()
        );
    }
}