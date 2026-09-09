package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Cobertura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoberturaRepository extends JpaRepository<Cobertura, Integer> {

    List<Cobertura> findByActivoTrue();

    List<Cobertura> findByPacienteIdAndActivoTrue(Integer pacienteId);

    Optional<Cobertura> findByPacienteIdAndFinanciadorIdAndNumeroAfiliado(
            Integer pacienteId, Integer financiadorId, String numeroAfiliado);

}
