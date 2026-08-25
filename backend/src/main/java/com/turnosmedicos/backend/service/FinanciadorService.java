package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.FinanciadorRequestDTO;
import com.turnosmedicos.backend.dto.FinanciadorResponseDTO;
import com.turnosmedicos.backend.entity.Financiador;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.FinanciadorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanciadorService {

    private final FinanciadorRepository financiadorRepository;

    public FinanciadorService(FinanciadorRepository financiadorRepository) {
        this.financiadorRepository = financiadorRepository;
    }

    public List<FinanciadorResponseDTO> listarActivos() {
        return financiadorRepository.findByActivoTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public FinanciadorResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    public FinanciadorResponseDTO crear(FinanciadorRequestDTO request) {
        financiadorRepository.findByNombreIgnoreCase(request.nombre()).ifPresent(f -> {
            throw new ConflictoException("Ya existe un financiador con el nombre '" + request.nombre() + "'");
        });

        Financiador financiador = Financiador.builder()
                .nombre(request.nombre())
                .cuit(request.cuit())
                .build();

        return toResponseDTO(financiadorRepository.save(financiador));
    }

    public FinanciadorResponseDTO actualizar(Integer id, FinanciadorRequestDTO request) {
        Financiador financiador = buscarPorId(id);

        financiadorRepository.findByNombreIgnoreCase(request.nombre())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ConflictoException("Ya existe un financiador con el nombre '" + request.nombre() + "'");
                });

        financiador.setNombre(request.nombre());
        financiador.setCuit(request.cuit());

        return toResponseDTO(financiadorRepository.save(financiador));
    }

    public void eliminar(Integer id) {
        Financiador financiador = buscarPorId(id);
        financiador.setActivo(false);
        financiadorRepository.save(financiador);
    }

    private Financiador buscarPorId(Integer id) {
        return financiadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el financiador con id " + id));
    }

    private FinanciadorResponseDTO toResponseDTO(Financiador financiador) {
        return new FinanciadorResponseDTO(
                financiador.getId(),
                financiador.getNombre(),
                financiador.getCuit(),
                financiador.getActivo()
        );
    }

}
