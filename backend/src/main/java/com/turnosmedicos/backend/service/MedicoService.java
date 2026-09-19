package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.MedicoRequestDTO;
import com.turnosmedicos.backend.dto.MedicoResponseDTO;
import com.turnosmedicos.backend.entity.Disponibilidad;
import com.turnosmedicos.backend.entity.Medico;
import com.turnosmedicos.backend.entity.Turno;
import com.turnosmedicos.backend.entity.Usuario;
import com.turnosmedicos.backend.enums.DiaSemana;
import com.turnosmedicos.backend.enums.Rol;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.BloqueoRepository;
import com.turnosmedicos.backend.repository.DisponibilidadRepository;
import com.turnosmedicos.backend.repository.MedicoRepository;
import com.turnosmedicos.backend.repository.TurnoRepository;
import com.turnosmedicos.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final TurnoRepository turnoRepository;
    private final BloqueoRepository bloqueoRepository;

    public MedicoService(
            MedicoRepository medicoRepository,
            UsuarioRepository usuarioRepository,
            DisponibilidadRepository disponibilidadRepository,
            TurnoRepository turnoRepository,
            BloqueoRepository bloqueoRepository
    ) {
        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.turnoRepository = turnoRepository;
        this.bloqueoRepository = bloqueoRepository;
    }

    public List<MedicoResponseDTO> listarActivos() {
        return medicoRepository.findByActivoTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public MedicoResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    public MedicoResponseDTO crear(MedicoRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id " + request.idUsuario()));

        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new ConflictoException("El usuario " + request.idUsuario() + " no tiene rol ADMINISTRADOR");
        }
        if (!usuario.getDni().equals(request.dni())) {
            throw new ConflictoException("El DNI del médico debe coincidir con el DNI del usuario");
        }
        if (medicoRepository.existsByUsuarioId(request.idUsuario())) {
            throw new ConflictoException("El usuario " + request.idUsuario() + " ya tiene un médico asociado");
        }
        if (medicoRepository.existsByDni(request.dni())) {
            throw new ConflictoException("Ya existe un médico con el DNI '" + request.dni() + "'");
        }
        medicoRepository.findByMatricula(request.matricula()).ifPresent(m -> {
            throw new ConflictoException("Ya existe un médico con la matrícula '" + request.matricula() + "'");
        });

        Medico medico = Medico.builder()
                .usuario(usuario)
                .nombreApellido(request.nombre())
                .dni(request.dni())
                .matricula(request.matricula())
                .especialidades(request.especialidad() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(List.of(request.especialidad())))
                .build();

        return toResponseDTO(medicoRepository.save(medico));
    }

    public void eliminar(Integer id) {
        Medico medico = buscarPorId(id);
        medico.setActivo(false);
        medicoRepository.save(medico);
    }

    public MedicoResponseDTO actualizar(Integer id, MedicoRequestDTO request) {
        Medico medico = buscarPorId(id);

        medicoRepository.findByMatricula(request.matricula())
                .filter(m -> !m.getId().equals(id))
                .ifPresent(m -> {
                    throw new ConflictoException("Ya existe un médico con la matrícula '" + request.matricula() + "'");
                });

        medico.setNombreApellido(request.nombre());
        medico.setMatricula(request.matricula());
        medico.setEspecialidades(request.especialidad() == null
                ? new ArrayList<>()
                : new ArrayList<>(List.of(request.especialidad())));

        return toResponseDTO(medicoRepository.save(medico));
    }

    public List<String> obtenerHorariosDisponibles(Integer id, LocalDate fecha) {
        if (fecha == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha es obligatoria.");
        }

        buscarPorId(id);

        boolean bloqueado = bloqueoRepository
                .existsByMedicoIdAndActivoTrueAndFechaDesdeLessThanEqualAndFechaHastaGreaterThanEqual(id, fecha, fecha);
        if (bloqueado) {
            return List.of();
        }

        DiaSemana dia = DiaSemana.values()[fecha.getDayOfWeek().getValue() - 1];

        List<Disponibilidad> disponibilidades = disponibilidadRepository
                .findByMedicoIdAndDiaSemanaAndActivoTrue(id, dia);

        if (disponibilidades.isEmpty()) {
            return List.of();
        }

        Set<LocalTime> ocupados = turnoRepository
                .findByMedicoIdAndFecha(id, fecha)
                .stream()
                .map(Turno::getHora)
                .collect(Collectors.toSet());

        List<String> horarios = new ArrayList<>();

        for (Disponibilidad disponibilidad : disponibilidades) {
            LocalTime inicio = disponibilidad.getHoraInicio();
            LocalTime fin = disponibilidad.getHoraFin();
            int paso = disponibilidad.getDuracionTurnoMin();

            while (!inicio.isAfter(fin.minusMinutes(paso))) {
                if (!ocupados.contains(inicio)) {
                    horarios.add(inicio.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
                inicio = inicio.plusMinutes(paso);
            }
        }

        return horarios;
    }

    private Medico buscarPorId(Integer id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el médico con id " + id));
    }

    private MedicoResponseDTO toResponseDTO(Medico medico) {
        return new MedicoResponseDTO(
                medico.getId(),
                medico.getNombreApellido(),
                medico.getMatricula(),
                medico.getEspecialidades().isEmpty() ? null : medico.getEspecialidades().get(0),
                medico.getActivo()
        );
    }
}
