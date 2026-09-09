package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.DisponibilidadRequestDTO;
import com.turnosmedicos.backend.dto.DisponibilidadResponseDTO;
import com.turnosmedicos.backend.entity.Disponibilidad;
import com.turnosmedicos.backend.entity.Medico;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.DisponibilidadRepository;
import com.turnosmedicos.backend.repository.MedicoRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class DisponibilidadService {

    private final DisponibilidadRepository disponibilidadRepository;
    private final MedicoRepository medicoRepository;

    public DisponibilidadService(DisponibilidadRepository disponibilidadRepository,
                                 MedicoRepository medicoRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.medicoRepository = medicoRepository;
    }

    public List<DisponibilidadResponseDTO> listarActivos() {
        return disponibilidadRepository.findByActivoTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<DisponibilidadResponseDTO> listarPorMedico(Integer idMedico) {
        buscarMedico(idMedico);
        return disponibilidadRepository.findByMedicoIdAndActivoTrue(idMedico).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public DisponibilidadResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    public DisponibilidadResponseDTO crear(DisponibilidadRequestDTO request) {
        Medico medico = buscarMedico(request.idMedico());
        if (!medico.getActivo()) {
            throw new ConflictoException("El médico " + request.idMedico() + " está dado de baja");
        }

        validarRangoHorario(request);
        validarSolapamiento(request, null);

        Disponibilidad disponibilidad = Disponibilidad.builder()
                .medico(medico)
                .diaSemana(request.diaSemana())
                .horaInicio(request.horaInicio())
                .horaFin(request.horaFin())
                .duracionTurnoMin(request.duracionTurnoMin())
                .build();

        return toResponseDTO(disponibilidadRepository.save(disponibilidad));
    }

    public DisponibilidadResponseDTO actualizar(Integer id, DisponibilidadRequestDTO request) {
        Disponibilidad disponibilidad = buscarPorId(id);
        Medico medico = buscarMedico(request.idMedico());

        validarRangoHorario(request);
        validarSolapamiento(request, id);

        disponibilidad.setMedico(medico);
        disponibilidad.setDiaSemana(request.diaSemana());
        disponibilidad.setHoraInicio(request.horaInicio());
        disponibilidad.setHoraFin(request.horaFin());
        disponibilidad.setDuracionTurnoMin(request.duracionTurnoMin());

        return toResponseDTO(disponibilidadRepository.save(disponibilidad));
    }

    public void eliminar(Integer id) {
        Disponibilidad disponibilidad = buscarPorId(id);
        disponibilidad.setActivo(false);
        disponibilidadRepository.save(disponibilidad);
    }

    private void validarRangoHorario(DisponibilidadRequestDTO request) {
        if (!request.horaInicio().isBefore(request.horaFin())) {
            throw new ConflictoException("La hora de inicio debe ser anterior a la hora de fin");
        }
        long minutosVentana = Duration.between(request.horaInicio(), request.horaFin()).toMinutes();
        if (request.duracionTurnoMin() > minutosVentana) {
            throw new ConflictoException("La duración del turno no entra en la ventana horaria indicada");
        }
    }

    private void validarSolapamiento(DisponibilidadRequestDTO request, Integer idAExcluir) {
        boolean seSolapa = disponibilidadRepository
                .findByMedicoIdAndDiaSemanaAndActivoTrue(request.idMedico(), request.diaSemana()).stream()
                .filter(otra -> !otra.getId().equals(idAExcluir))
                .anyMatch(otra -> request.horaInicio().isBefore(otra.getHoraFin())
                        && otra.getHoraInicio().isBefore(request.horaFin()));
        if (seSolapa) {
            throw new ConflictoException("El médico ya tiene una disponibilidad que se solapa con ese horario");
        }
    }

    private Disponibilidad buscarPorId(Integer id) {
        return disponibilidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la disponibilidad con id " + id));
    }

    private Medico buscarMedico(Integer id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el médico con id " + id));
    }

    private DisponibilidadResponseDTO toResponseDTO(Disponibilidad disponibilidad) {
        return new DisponibilidadResponseDTO(
                disponibilidad.getId(),
                disponibilidad.getMedico().getId(),
                disponibilidad.getDiaSemana(),
                disponibilidad.getHoraInicio(),
                disponibilidad.getHoraFin(),
                disponibilidad.getDuracionTurnoMin(),
                disponibilidad.getActivo()
        );
    }

}
