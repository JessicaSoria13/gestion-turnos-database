package com.turnosmedicos.backend.entity;

import com.turnosmedicos.backend.enums.EstadoTurno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

// Sin campo "activo": el turno nunca se elimina (ni fisica ni logicamente);
// su ciclo de vida se maneja con el enum estado. No expone DELETE.
//
// La unicidad (id_medico, fecha, hora) NO se declara aca como @UniqueConstraint
// de Hibernate porque tiene que excluir los turnos CANCELADO (un horario
// cancelado se vuelve a ofrecer). Se aplica como indice
// unico parcial a mano en db/migracion-2026-09.sql, porque ddl-auto=update no
// puede generar un indice condicional (WHERE) desde las anotaciones.
@Entity
@Table(name = "turno")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_medico", nullable = false)
    private Medico medico;

    @ManyToOne
    @JoinColumn(name = "id_disponibilidad", nullable = false)
    private Disponibilidad disponibilidad;

    // NULL = atencion particular.
    @ManyToOne
    @JoinColumn(name = "id_cobertura")
    private Cobertura cobertura;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTurno estado = EstadoTurno.CONFIRMADO;

    @Column(name = "motivo_consulta", length = 255)
    private String motivoConsulta;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

}
