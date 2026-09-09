package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Integer> {

    List<Turno> findByMedicoIdAndFecha(Integer medicoId, LocalDate fecha);
}
