package com.ebbenezer.taller.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true)
    private String sku;

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(nullable = false)
    private BigDecimal precio;

    private String categoria;

    @Column(columnDefinition = "TEXT")
    private String imagen;
}
