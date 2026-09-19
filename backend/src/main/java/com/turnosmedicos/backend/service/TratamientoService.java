package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.TratamientoRequestDTO;
import com.turnosmedicos.backend.dto.TratamientoResponseDTO;
import com.turnosmedicos.backend.entity.Diagnostico;
import com.turnosmedicos.backend.entity.Tratamiento;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.DiagnosticoRepository;
import com.turnosmedicos.backend.repository.TratamientoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TratamientoService {

    private final TratamientoRepository tratamientoRepository;
    private final DiagnosticoRepository diagnosticoRepository;

    public TratamientoService(TratamientoRepository tratamientoRepository,
                              DiagnosticoRepository diagnosticoRepository) {
        this.tratamientoRepository = tratamientoRepository;
        this.diagnosticoRepository = diagnosticoRepository;
    }

    public List<TratamientoResponseDTO> listar(Integer idDiagnostico) {
        if (idDiagnostico != null) {
            buscarDiagnostico(idDiagnostico);
            return tratamientoRepository.findByDiagnosticoId(idDiagnostico).stream()
                    .map(this::toResponseDTO)
                    .toList();
        }

        return tratamientoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public TratamientoResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    public TratamientoResponseDTO crear(TratamientoRequestDTO request) {
        Diagnostico diagnostico = buscarDiagnostico(request.idDiagnostico());

        Tratamiento tratamiento = Tratamiento.builder()
                .diagnostico(diagnostico)
                .descripcion(request.descripcion())
                .duracion(request.duracion())
                .build();

        return toResponseDTO(tratamientoRepository.save(tratamiento));
    }

    public TratamientoResponseDTO actualizar(Integer id, TratamientoRequestDTO request) {
        Tratamiento tratamiento = buscarPorId(id);
        Diagnostico diagnostico = buscarDiagnostico(request.idDiagnostico());

        tratamiento.setDiagnostico(diagnostico);
        tratamiento.setDescripcion(request.descripcion());
        tratamiento.setDuracion(request.duracion());

        return toResponseDTO(tratamientoRepository.save(tratamiento));
    }

    private Tratamiento buscarPorId(Integer id) {
        return tratamientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el tratamiento con id " + id));
    }

    private Diagnostico buscarDiagnostico(Integer id) {
        return diagnosticoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el diagnóstico con id " + id));
    }

    private TratamientoResponseDTO toResponseDTO(Tratamiento tratamiento) {
        return new TratamientoResponseDTO(
                tratamiento.getId(),
                tratamiento.getDiagnostico().getId(),
                tratamiento.getDescripcion(),
                tratamiento.getDuracion(),
                tratamiento.getEstado()
        );
    }

}
