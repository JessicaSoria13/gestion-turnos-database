package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Cobertura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoberturaRepository extends JpaRepository<Cobertura, Integer> {
}
