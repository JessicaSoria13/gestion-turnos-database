package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnoRepository extends JpaRepository<Turno, Integer> {
}
