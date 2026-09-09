package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {

    List<Medico> findByActivoTrue();

    Optional<Medico> findByMatricula(String matricula);

}
