package com.ebbenezer.taller.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    private String direccion;

    // Score de confianza: sube con pagos puntuales, baja con atrasos.
    // Se usa para decidir si se le puede fiar mas facil.
    @Column(name = "score_confianza")
    private Integer scoreConfianza = 50;

    @Column(name = "limite_credito")
    private java.math.BigDecimal limiteCredito = java.math.BigDecimal.ZERO;

    @Column(name = "saldo_pendiente")
    private java.math.BigDecimal saldoPendiente = java.math.BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean activo = true;
}
