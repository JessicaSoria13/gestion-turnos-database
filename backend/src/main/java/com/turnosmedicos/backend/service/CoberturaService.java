package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.CoberturaRequestDTO;
import com.turnosmedicos.backend.dto.CoberturaResponseDTO;
import com.turnosmedicos.backend.entity.Cobertura;
import com.turnosmedicos.backend.entity.Financiador;
import com.turnosmedicos.backend.entity.Paciente;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.CoberturaRepository;
import com.turnosmedicos.backend.repository.FinanciadorRepository;
import com.turnosmedicos.backend.repository.PacienteRepository;
import com.turnosmedicos.backend.security.UsuarioActual;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoberturaService {

    private final CoberturaRepository coberturaRepository;
    private final PacienteRepository pacienteRepository;
    private final FinanciadorRepository financiadorRepository;
    private final UsuarioActual usuarioActual;

    public CoberturaService(CoberturaRepository coberturaRepository,
                            PacienteRepository pacienteRepository,
                            FinanciadorRepository financiadorRepository,
                            UsuarioActual usuarioActual) {
        this.coberturaRepository = coberturaRepository;
        this.pacienteRepository = pacienteRepository;
        this.financiadorRepository = financiadorRepository;
        this.usuarioActual = usuarioActual;
    }

    // Todas las coberturas del sistema: solo ADMINISTRADOR.
    // El PACIENTE usa GET /coberturas?idPaciente={su id}.
    public List<CoberturaResponseDTO> listarActivos() {
        usuarioActual.verificarAdministrador(
                "Solo el administrador puede listar todas las coberturas; indicá idPaciente");
        return coberturaRepository.findByActivoTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<CoberturaResponseDTO> listarPorPaciente(Integer idPaciente) {
        usuarioActual.verificarPaciente(buscarPaciente(idPaciente));
        return coberturaRepository.findByPacienteIdAndActivoTrue(idPaciente).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CoberturaResponseDTO obtenerPorId(Integer id) {
        Cobertura cobertura = buscarPorId(id);
        usuarioActual.verificarPaciente(cobertura.getPaciente());
        return toResponseDTO(cobertura);
    }

    public CoberturaResponseDTO crear(CoberturaRequestDTO request) {
        Paciente paciente = buscarPaciente(request.idPaciente());
        usuarioActual.verificarPaciente(paciente);
        if (!paciente.getActivo()) {
            throw new ConflictoException("El paciente " + request.idPaciente() + " está dado de baja");
        }

        Financiador financiador = buscarFinanciador(request.idFinanciador());
        if (!financiador.getActivo()) {
            throw new ConflictoException("El financiador " + request.idFinanciador() + " está dado de baja");
        }

        validarUnicidad(request, null);

        Cobertura cobertura = Cobertura.builder()
                .paciente(paciente)
                .financiador(financiador)
                .numeroAfiliado(request.numeroAfiliado())
                .plan(request.plan())
                .build();

        return toResponseDTO(coberturaRepository.save(cobertura));
    }

    public CoberturaResponseDTO actualizar(Integer id, CoberturaRequestDTO request) {
        Cobertura cobertura = buscarPorId(id);
        usuarioActual.verificarPaciente(cobertura.getPaciente());

        if (!cobertura.getPaciente().getId().equals(request.idPaciente())) {
            throw new ConflictoException("No se puede cambiar el paciente de una cobertura");
        }

        Financiador financiador = buscarFinanciador(request.idFinanciador());

        validarUnicidad(request, id);

        cobertura.setFinanciador(financiador);
        cobertura.setNumeroAfiliado(request.numeroAfiliado());
        cobertura.setPlan(request.plan());

        return toResponseDTO(coberturaRepository.save(cobertura));
    }

    public void eliminar(Integer id) {
        Cobertura cobertura = buscarPorId(id);
        usuarioActual.verificarPaciente(cobertura.getPaciente());
        cobertura.setActivo(false);
        coberturaRepository.save(cobertura);
    }

    private void validarUnicidad(CoberturaRequestDTO request, Integer idAExcluir) {
        coberturaRepository.findByPacienteIdAndFinanciadorIdAndNumeroAfiliado(
                        request.idPaciente(), request.idFinanciador(), request.numeroAfiliado())
                .filter(otra -> !otra.getId().equals(idAExcluir))
                .ifPresent(otra -> {
                    throw new ConflictoException("El paciente ya tiene una cobertura con ese financiador y número de afiliado");
                });
    }

    private Cobertura buscarPorId(Integer id) {
        return coberturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la cobertura con id " + id));
    }

    private Paciente buscarPaciente(Integer id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el paciente con id " + id));
    }

    private Financiador buscarFinanciador(Integer id) {
        return financiadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el financiador con id " + id));
    }

    private CoberturaResponseDTO toResponseDTO(Cobertura cobertura) {
        return new CoberturaResponseDTO(
                cobertura.getId(),
                cobertura.getPaciente().getId(),
                cobertura.getFinanciador().getId(),
                cobertura.getNumeroAfiliado(),
                cobertura.getPlan(),
                cobertura.getActivo()
        );
    }

}
