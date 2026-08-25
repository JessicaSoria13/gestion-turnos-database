package com.turnosmedicos.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medico")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "nombre_apellido", length = 100, nullable = false)
    private String nombreApellido;

    @Column(name = "dni", length = 10, nullable = false, unique = true)
    private String dni;

    @Column(name = "matricula", length = 20, nullable = false, unique = true)
    private String matricula;

    // Un medico puede tener varias especialidades (ver contexto.md).
    // Se modela en tabla aparte medico_especialidad, no como columna unica.
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "medico_especialidad", joinColumns = @JoinColumn(name = "id_medico"))
    @Column(name = "especialidad", length = 100, nullable = false)
    private List<String> especialidades = new ArrayList<>();

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

}
