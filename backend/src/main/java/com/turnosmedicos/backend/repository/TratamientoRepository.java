package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TratamientoRepository extends JpaRepository<Tratamiento, Integer> {
}
