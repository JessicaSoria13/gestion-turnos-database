package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.BloqueoRequestDTO;
import com.turnosmedicos.backend.dto.BloqueoResponseDTO;
import com.turnosmedicos.backend.entity.Bloqueo;
import com.turnosmedicos.backend.entity.Medico;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.BloqueoRepository;
import com.turnosmedicos.backend.repository.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BloqueoService {

    private final BloqueoRepository bloqueoRepository;
    private final MedicoRepository medicoRepository;

    public BloqueoService(BloqueoRepository bloqueoRepository, MedicoRepository medicoRepository) {
        this.bloqueoRepository = bloqueoRepository;
        this.medicoRepository = medicoRepository;
    }

    public List<BloqueoResponseDTO> listarActivos() {
        return bloqueoRepository.findByActivoTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<BloqueoResponseDTO> listarPorMedico(Integer idMedico) {
        buscarMedico(idMedico);
        return bloqueoRepository.findByMedicoIdAndActivoTrue(idMedico).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public BloqueoResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    public BloqueoResponseDTO crear(BloqueoRequestDTO request) {
        Medico medico = buscarMedico(request.idMedico());
        if (!medico.getActivo()) {
            throw new ConflictoException("El médico " + request.idMedico() + " está dado de baja");
        }

        validarRangoFechas(request);

        Bloqueo bloqueo = Bloqueo.builder()
                .medico(medico)
                .fechaDesde(request.fechaDesde())
                .fechaHasta(request.fechaHasta())
                .motivo(request.motivo())
                .build();

        Bloqueo guardado = bloqueoRepository.save(bloqueo);

        // TODO (regla 9, contexto.md): al crear el bloqueo se deben cancelar automáticamente
        // los turnos CONFIRMADO del médico cuya fecha caiga en [fechaDesde, fechaHasta],
        // propagando `motivo` como motivoCancelacion. Se implementa cuando exista el módulo Turno.

        return toResponseDTO(guardado);
    }

    public BloqueoResponseDTO actualizar(Integer id, BloqueoRequestDTO request) {
        Bloqueo bloqueo = buscarPorId(id);
        Medico medico = buscarMedico(request.idMedico());

        validarRangoFechas(request);

        bloqueo.setMedico(medico);
        bloqueo.setFechaDesde(request.fechaDesde());
        bloqueo.setFechaHasta(request.fechaHasta());
        bloqueo.setMotivo(request.motivo());

        return toResponseDTO(bloqueoRepository.save(bloqueo));
    }

    public void eliminar(Integer id) {
        // Baja lógica: vuelve a ofrecer los horarios del rango, pero NO restaura
        // los turnos que el bloqueo hubiera cancelado (regla 9, contexto.md).
        Bloqueo bloqueo = buscarPorId(id);
        bloqueo.setActivo(false);
        bloqueoRepository.save(bloqueo);
    }

    private void validarRangoFechas(BloqueoRequestDTO request) {
        if (request.fechaHasta().isBefore(request.fechaDesde())) {
            throw new ConflictoException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    private Bloqueo buscarPorId(Integer id) {
        return bloqueoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el bloqueo con id " + id));
    }

    private Medico buscarMedico(Integer id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el médico con id " + id));
    }

    private BloqueoResponseDTO toResponseDTO(Bloqueo bloqueo) {
        return new BloqueoResponseDTO(
                bloqueo.getId(),
                bloqueo.getMedico().getId(),
                bloqueo.getFechaDesde(),
                bloqueo.getFechaHasta(),
                bloqueo.getMotivo(),
                bloqueo.getActivo()
        );
    }

}
