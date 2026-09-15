package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Bloqueo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BloqueoRepository extends JpaRepository<Bloqueo, Integer> {

    List<Bloqueo> findByActivoTrue();

    List<Bloqueo> findByMedicoIdAndActivoTrue(Integer medicoId);

    // fechaParaDesde y fechaParaHasta reciben el mismo valor: el bloqueo cubre la fecha
    // si fechaDesde <= fecha <= fechaHasta.
    boolean existsByMedicoIdAndActivoTrueAndFechaDesdeLessThanEqualAndFechaHastaGreaterThanEqual(
            Integer medicoId, LocalDate fechaParaDesde, LocalDate fechaParaHasta);

}
