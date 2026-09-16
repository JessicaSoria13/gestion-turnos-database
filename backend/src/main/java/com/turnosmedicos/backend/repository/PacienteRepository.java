package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {

	List<Paciente> findByActivoTrue();

	boolean existsByUsuarioId(Integer usuarioId);

	boolean existsByDni(String dni);

    Optional<Paciente> findByDni(String dni);

    Optional<Paciente> findByEmail(String email);
}
