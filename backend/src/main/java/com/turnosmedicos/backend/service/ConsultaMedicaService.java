package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.ConsultaMedicaRequestDTO;
import com.turnosmedicos.backend.dto.ConsultaMedicaResponseDTO;
import com.turnosmedicos.backend.entity.ConsultaMedica;
import com.turnosmedicos.backend.entity.Turno;
import com.turnosmedicos.backend.enums.EstadoTurno;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.ConsultaMedicaRepository;
import com.turnosmedicos.backend.repository.TurnoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service

public class ConsultaMedicaService {

    private final ConsultaMedicaRepository consultaMedicaRepository;
    private final TurnoRepository turnoRepository;

    public ConsultaMedicaService(ConsultaMedicaRepository consultaMedicaRepository, TurnoRepository turnoRepository) {
        this.consultaMedicaRepository = consultaMedicaRepository;
        this.turnoRepository = turnoRepository;
    }

    public List<ConsultaMedicaResponseDTO> listar() {
        return consultaMedicaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }
    public ConsultaMedicaResponseDTO obtenerPorId(Integer id) {
        ConsultaMedica consulta = consultaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe consulta medica con el id: " + id));
        return toResponseDTO(consulta);
    }

    public ConsultaMedicaResponseDTO crear(ConsultaMedicaRequestDTO request) {
        Turno turno = turnoRepository.findById(request.idTurno())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe turno con el id: " + request.idTurno()));

        if (turno.getEstado() != EstadoTurno.CONFIRMADO) {
            throw new ConflictoException("Solo se puede registrar una consulta a partir de un turno en estado CONFIRMADO");
        }
        if (consultaMedicaRepository.existsByTurnoId(request.idTurno())) {
            throw new ConflictoException("El turno ya tiene una consulta medica registrada");
        }
        
        ConsultaMedica consulta = ConsultaMedica.builder()
                .turno(turno)
                .observaciones(request.observaciones())
                .build();
        consultaMedicaRepository.save(consulta);

        turno.setEstado(EstadoTurno.ATENDIDO);
        turnoRepository.save(turno);

        return toResponseDTO(consulta);

    }

    private ConsultaMedicaResponseDTO toResponseDTO(ConsultaMedica consulta) {
    return  new ConsultaMedicaResponseDTO(
            consulta.getId(),
            consulta.getTurno().getId(),
            consulta.getTurno().getFecha(),
            consulta.getTurno().getHora(),
            consulta.getObservaciones()
    );
    }
}
