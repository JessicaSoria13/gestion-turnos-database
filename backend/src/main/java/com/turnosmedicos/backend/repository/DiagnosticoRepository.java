package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Integer> {

	List<Diagnostico> findByConsulta_Id(Integer consultaId);

}
