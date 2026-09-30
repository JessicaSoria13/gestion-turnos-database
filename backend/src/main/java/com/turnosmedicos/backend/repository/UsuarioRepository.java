package com.turnosmedicos.backend.repository;

import com.turnosmedicos.backend.entity.Usuario;
import com.turnosmedicos.backend.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findByActivoTrue();

    // Un dni puede tener una fila PACIENTE y otra ADMINISTRADOR: puede devolver
    // mas de un resultado, a diferencia de antes.
    List<Usuario> findByDni(String dni);

    Optional<Usuario> findByDniAndRol(String dni, Rol rol);

}
