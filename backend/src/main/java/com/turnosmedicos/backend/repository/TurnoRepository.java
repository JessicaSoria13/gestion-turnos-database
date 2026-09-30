package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Turno;
import com.turnosmedicos.backend.enums.EstadoTurno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Integer> {

    // Un turno CANCELADO no ocupa el horario (contexto.md, seccion 9, paso 4):
    // este metodo lo excluye explicitamente para el calculo de horarios libres
    // y para el chequeo de choque al crear un turno nuevo.
    List<Turno> findByMedicoIdAndFechaAndEstadoNot(Integer medicoId, LocalDate fecha, EstadoTurno estadoExcluido);

    boolean existsByMedicoIdAndFechaAndHoraAndEstadoNot(Integer medicoId, LocalDate fecha, LocalTime hora, EstadoTurno estadoExcluido);

    List<Turno> findByMedicoIdAndEstadoAndFechaBetween(Integer medicoId, EstadoTurno estado, LocalDate fechaDesde, LocalDate fechaHasta);

}
