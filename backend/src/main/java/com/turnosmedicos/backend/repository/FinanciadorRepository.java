package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Financiador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinanciadorRepository extends JpaRepository<Financiador, Integer> {

    List<Financiador> findByActivoTrue();

    Optional<Financiador> findByNombreIgnoreCase(String nombre);

}
