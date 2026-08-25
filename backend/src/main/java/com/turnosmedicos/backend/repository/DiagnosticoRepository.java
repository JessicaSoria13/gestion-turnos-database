package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Integer> {
}
