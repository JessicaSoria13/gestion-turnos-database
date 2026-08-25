package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
}
