package com.turnosmedicos.backend.service;

import com.turnosmedicos.backend.dto.MedicoRequestDTO;
import com.turnosmedicos.backend.dto.MedicoResponseDTO;
import com.turnosmedicos.backend.entity.Disponibilidad;
import com.turnosmedicos.backend.entity.Medico;
import com.turnosmedicos.backend.entity.Turno;
import com.turnosmedicos.backend.exception.ConflictoException;
import com.turnosmedicos.backend.exception.RecursoNoEncontradoException;
import com.turnosmedicos.backend.repository.DisponibilidadRepository;
import com.turnosmedicos.backend.repository.MedicoRepository;
import com.turnosmedicos.backend.repository.TurnoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
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
    private final DisponibilidadRepository disponibilidadRepository;
    private final TurnoRepository turnoRepository;

    public MedicoService(
            MedicoRepository medicoRepository,
            DisponibilidadRepository disponibilidadRepository,
            TurnoRepository turnoRepository
    ) {
        this.medicoRepository = medicoRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.turnoRepository = turnoRepository;
    }

    public MedicoResponseDTO obtenerPorId(Integer id) {
        return toResponseDTO(buscarPorId(id));
    }

    public MedicoResponseDTO actualizar(Integer id, MedicoRequestDTO request) {
        Medico medico = buscarPorId(id);

        medicoRepository.findByMatricula(request.matricula())
                .filter(m -> !m.getId().equals(id))
                .ifPresent(m -> {
                    throw new ConflictoException("Ya existe un médico con la matrícula '" + request.matricula() + "'");
                });

        medico.setNombre(request.nombre());
        medico.setMatricula(request.matricula());
        medico.setEspecialidad(request.especialidad());

        return toResponseDTO(medicoRepository.save(medico));
    }

    public List<String> obtenerHorariosDisponibles(Integer id, LocalDate fecha) {
        if (fecha == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha es obligatoria.");
        }

        buscarPorId(id);

        // Si existe bloqueo activo para ese médico y esa fecha, devolvemos vacío
        // if (bloqueoRepository.existsByMedicoIdAndFechaAndActivoTrue(id, fecha)) {
        //     return List.of();
        // }

        DayOfWeek dia = fecha.getDayOfWeek();

        List<Disponibilidad> disponibilidades = disponibilidadRepository
                .findByMedicoIdAndDiaSemana(id, dia);

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

            while (!inicio.isAfter(fin.minusMinutes(30))) {
                if (!ocupados.contains(inicio)) {
                    horarios.add(inicio.format(DateTimeFormatter.ofPattern("HH:mm")));
                }
                inicio = inicio.plusMinutes(30);
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
                medico.getNombre(),
                medico.getMatricula(),
                medico.getEspecialidad(),
                medico.getActivo()
        );
    }
}
