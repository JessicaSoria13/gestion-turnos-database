package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {
}
