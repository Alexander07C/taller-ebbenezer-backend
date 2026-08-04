package com.ebbenezer.taller.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ordenes_trabajo")
@Getter
@Setter
@NoArgsConstructor
public class OrdenTrabajo {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.RECIBIDO;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso = LocalDateTime.now();

    private LocalDateTime fechaEntrega;

    public enum Estado {
        RECIBIDO,
        EN_REPARACION,
        LISTO,
        ENTREGADO
    }
}
