package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

	List<Paciente> findByActivoTrue();

	boolean existsByUsuarioId(Integer usuarioId);

	boolean existsByDni(String dni);
}
