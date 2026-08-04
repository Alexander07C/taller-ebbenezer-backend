package com.ebbenezer.taller.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "numero_venta", unique = true)
    private Long numeroVenta;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private BigDecimal total;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @Column(name = "es_fiado", nullable = false)
    private boolean esFiado = false;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Abono> abonos = new ArrayList<>();
}
