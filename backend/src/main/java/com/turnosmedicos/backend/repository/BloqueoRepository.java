package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloqueoRepository extends JpaRepository<Bloqueo, Integer> {

    List<Bloqueo> findByActivoTrue();

    List<Bloqueo> findByMedicoIdAndActivoTrue(Integer medicoId);

}
