package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.TurnoRequestDTO;
import com.turnosmedicos.backend.dto.TurnoResponseDTO;
import com.turnosmedicos.backend.entity.Cobertura;
import com.turnosmedicos.backend.entity.Disponibilidad;
import com.turnosmedicos.backend.entity.Medico;
import com.turnosmedicos.backend.entity.Paciente;
import com.turnosmedicos.backend.entity.Turno;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.CoberturaRepository;
import com.turnosmedicos.backend.repository.DisponibilidadRepository;
import com.turnosmedicos.backend.repository.MedicoRepository;
import com.turnosmedicos.backend.repository.PacienteRepository;
import com.turnosmedicos.backend.repository.TurnoRepository;
import com.turnosmedicos.backend.dto.TurnoCancelacionRequestDTO;
import com.turnosmedicos.backend.enums.DiaSemana;
import com.turnosmedicos.backend.enums.EstadoTurno;
import com.turnosmedicos.backend.exception.DatosInvalidosException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Service
public class TurnoService {
    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final DisponibilidadRepository disponibilidadRepository;
    private final CoberturaRepository coberturaRepository;

    public TurnoService(
            TurnoRepository turnoRepository,
            PacienteRepository pacienteRepository,
            MedicoRepository medicoRepository,
            DisponibilidadRepository disponibilidadRepository,
            CoberturaRepository coberturaRepository) {
            this.turnoRepository = turnoRepository;
            this.pacienteRepository = pacienteRepository;
            this.medicoRepository = medicoRepository;
            this.disponibilidadRepository = disponibilidadRepository;
            this.coberturaRepository = coberturaRepository;
    }

    public List<TurnoResponseDTO> listar() {
        return turnoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public TurnoResponseDTO obtenerPorId(Integer id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe turno con el id: " + id));
        return toResponseDTO(turno);
    }

    // El token solo trae el dni, no el id de paciente: lo resuelve a partir
    // del dni del usuario autenticado (igual que PacienteService.obtenerPropio).
    public List<TurnoResponseDTO> misTurnos() {
        String dni = SecurityContextHolder.getContext().getAuthentication().getName();
        Paciente paciente = pacienteRepository.findByDni(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un paciente asociado al dni " + dni));

        return turnoRepository.findByPacienteIdOrderByFechaDescHoraDesc(paciente.getId()).stream()
                .map(this::toResponseDTO)
                .toList();
    }
    public TurnoResponseDTO cancelar(Integer id, TurnoCancelacionRequestDTO request) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe turno con el id: " + id));

        if (turno.getEstado() != EstadoTurno.CONFIRMADO) {
            throw new ConflictoException("Solo se puede cancelar un turno en estado CONFIRMADO");
        }

        if (request.canceladoPorMedico() && (request.motivoCancelacion() == null || request.motivoCancelacion().isBlank())) {
            throw new DatosInvalidosException("El motivo de cancelación es obligatorio cuando cancela el médico");
        }

        turno.setEstado(EstadoTurno.CANCELADO);
        turno.setMotivoCancelacion(request.motivoCancelacion());

        return toResponseDTO(turnoRepository.save(turno));
    }







    public TurnoResponseDTO crear(TurnoRequestDTO request) {
        Paciente paciente = pacienteRepository.findById(request.idPaciente())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe paciente con el id: " + request.idPaciente()));
        if (!paciente.getActivo()) {
            throw new ConflictoException("El paciente " + request.idPaciente() + " está dado de baja");
        }

        Medico medico = medicoRepository.findById(request.idMedico())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe medico con el id: " + request.idMedico()));
        if (!medico.getActivo()) {
            throw new ConflictoException("El médico " + request.idMedico() + " está dado de baja");
        }

        Disponibilidad disponibilidad = disponibilidadRepository.findById(request.idDisponibilidad())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe disponibilidad con el id: " + request.idDisponibilidad()));

        if (!disponibilidad.getActivo()) {
            throw new ConflictoException("La disponibilidad indicada está dada de baja");
        }
        if (!disponibilidad.getMedico().getId().equals(medico.getId())) {
            throw new ConflictoException("La disponibilidad indicada no pertenece al médico");
        }

        DiaSemana diaDeLaFecha = DiaSemana.values()[request.fecha().getDayOfWeek().getValue() - 1];
        if (disponibilidad.getDiaSemana() != diaDeLaFecha) {
            throw new ConflictoException("La fecha no coincide con el día de la disponibilidad indicada");
        }

        LocalTime horaInicio = disponibilidad.getHoraInicio();
        LocalTime horaFin = disponibilidad.getHoraFin();
        if (request.hora().isBefore(horaInicio) || !request.hora().isBefore(horaFin)) {
            throw new ConflictoException("El horario está fuera de la disponibilidad del médico");
        }
        long minutosDesdeInicio = Duration.between(horaInicio, request.hora()).toMinutes();
        if (minutosDesdeInicio % disponibilidad.getDuracionTurnoMin() != 0) {
            throw new ConflictoException("El horario no coincide con turnos de " + disponibilidad.getDuracionTurnoMin() + " minutos");
        }

        Cobertura cobertura = null;
        if (request.idCobertura() != null) {
            cobertura = coberturaRepository.findById(request.idCobertura())
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe cobertura: " + request.idCobertura()));
        }

        if (turnoRepository.existsByMedicoIdAndFechaAndHoraAndEstadoNot(
                request.idMedico(), request.fecha(), request.hora(), EstadoTurno.CANCELADO)) {
            throw new ConflictoException("El medico ya tiene turno asignado en ese horario");
        }

        Turno turno = Turno.builder()
                .paciente(paciente)
                .medico(medico)
                .disponibilidad(disponibilidad)
                .cobertura(cobertura)
                .fecha(request.fecha())
                .hora(request.hora())
                .motivoConsulta(request.motivoConsulta())
                .build();

        return toResponseDTO(turnoRepository.save(turno));
    }

        private TurnoResponseDTO toResponseDTO(Turno turno) {
        return new TurnoResponseDTO(
                turno.getId(),
                turno.getPaciente().getId(),
                turno.getMedico().getId(),
                turno.getDisponibilidad().getId(),
                turno.getCobertura() != null ? turno.getCobertura().getId() : null,
                turno.getFecha(),
                turno.getHora(),
                turno.getEstado(),
                turno.getMotivoConsulta(),
                turno.getMotivoCancelacion()
        );

    }
}
