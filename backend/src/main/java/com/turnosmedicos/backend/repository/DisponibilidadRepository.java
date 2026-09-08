package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Integer> {

    List<Disponibilidad> findByMedicoIdAndDiaSemana(Integer medicoId, DayOfWeek diaSemana);
}
