package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {

	List<Tratamiento> findByDiagnosticoId(Integer diagnosticoId);

}
