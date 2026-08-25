package com.turnosmedicos.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cobertura", uniqueConstraints = {
        @UniqueConstraint(name = "uq_cobertura",
                columnNames = {"id_paciente", "id_financiador", "numero_afiliado"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cobertura")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_financiador", nullable = false)
    private Financiador financiador;

    @Column(name = "numero_afiliado", length = 30, nullable = false)
    private String numeroAfiliado;

    @Column(name = "plan", length = 50)
    private String plan;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

}
