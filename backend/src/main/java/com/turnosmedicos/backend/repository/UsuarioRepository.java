package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findByActivoTrue();

    Optional<Usuario> findByDni(String dni);

}
