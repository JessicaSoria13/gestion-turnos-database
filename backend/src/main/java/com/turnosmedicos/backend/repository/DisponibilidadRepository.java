package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Disponibilidad;
import com.turnosmedicos.backend.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Integer> {

    List<Disponibilidad> findByActivoTrue();

    List<Disponibilidad> findByMedicoIdAndActivoTrue(Integer medicoId);

    List<Disponibilidad> findByMedicoIdAndDiaSemanaAndActivoTrue(Integer medicoId, DiaSemana diaSemana);

}
