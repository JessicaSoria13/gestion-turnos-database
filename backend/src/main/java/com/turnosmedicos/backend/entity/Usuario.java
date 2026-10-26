package com.turnosmedicos.backend.entity;

import com.turnosmedicos.backend.enums.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un mismo DNI puede tener dos filas de Usuario (una PACIENTE y otra
// ADMINISTRADOR) - por eso la unicidad es sobre
// (dni, rol), no sobre dni solo. Ver db/migracion-2026-09.sql: quien ya
// tenia una base local con dni unico tiene que correr esa migracion a mano,
// ddl-auto=update no reemplaza una unique constraint existente por otra.
@Entity
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(name = "uq_usuario_dni_rol", columnNames = {"dni", "rol"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "dni", length = 10, nullable = false)
    private String dni;

    @Column(name = "contrasena", length = 255, nullable = false)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

}
