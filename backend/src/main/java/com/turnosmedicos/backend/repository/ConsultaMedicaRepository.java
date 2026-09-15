package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.ConsultaMedica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaMedicaRepository extends JpaRepository<ConsultaMedica, Integer> {

    boolean existsByTurnoId(Integer turnoId);
}
