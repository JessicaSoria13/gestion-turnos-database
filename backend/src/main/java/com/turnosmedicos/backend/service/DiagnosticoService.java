package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.DiagnosticoRequestDTO;
import com.turnosmedicos.backend.dto.DiagnosticoResponseDTO;
import com.turnosmedicos.backend.entity.ConsultaMedica;
import com.turnosmedicos.backend.entity.Diagnostico;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.ConsultaMedicaRepository;
import com.turnosmedicos.backend.repository.DiagnosticoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnosticoService {

    private final DiagnosticoRepository diagnosticoRepository;
    private final ConsultaMedicaRepository consultaMedicaRepository;

    public DiagnosticoService(DiagnosticoRepository diagnosticoRepository,
                              ConsultaMedicaRepository consultaMedicaRepository) {
        this.diagnosticoRepository = diagnosticoRepository;
        this.consultaMedicaRepository = consultaMedicaRepository;
    }

    public List<DiagnosticoResponseDTO> listar() {
        return diagnosticoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public DiagnosticoResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    public List<DiagnosticoResponseDTO> listarPorConsulta(Integer consultaId) {
        buscarConsultaPorId(consultaId);

        return diagnosticoRepository.findByConsulta_Id(consultaId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public DiagnosticoResponseDTO crear(DiagnosticoRequestDTO request) {
        ConsultaMedica consulta = buscarConsultaPorId(request.consultaId());

        Diagnostico diagnostico = Diagnostico.builder()
                .consulta(consulta)
                .descripcion(request.descripcion())
                .build();

        return toResponseDTO(diagnosticoRepository.save(diagnostico));
    }

    public DiagnosticoResponseDTO actualizar(Integer id, DiagnosticoRequestDTO request) {
        Diagnostico diagnostico = buscarPorId(id);
        ConsultaMedica consulta = buscarConsultaPorId(request.consultaId());

        diagnostico.setConsulta(consulta);
        diagnostico.setDescripcion(request.descripcion());

        return toResponseDTO(diagnosticoRepository.save(diagnostico));
    }

    private Diagnostico buscarPorId(Integer id) {
        return diagnosticoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el diagnóstico con id " + id));
    }

    private ConsultaMedica buscarConsultaPorId(Integer id) {
        return consultaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la consulta médica con id " + id));
    }

    private DiagnosticoResponseDTO toResponseDTO(Diagnostico diagnostico) {
        return new DiagnosticoResponseDTO(
                diagnostico.getId(),
                diagnostico.getConsulta().getId(),
                diagnostico.getDescripcion()
        );
    }

}
