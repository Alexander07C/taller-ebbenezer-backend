package com.ebbenezer.taller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SalidaRequest {

    private LocalDate fecha;

    @PositiveOrZero
    private BigDecimal montoServicio = BigDecimal.ZERO;

    private String concepto;

    private UUID clienteId;

    private String nombreCliente;

    private List<ItemRequest> productos;

    @NotNull
    private Boolean pagada = false;

    @Getter
    @Setter
    public static class ItemRequest {
        @NotNull
        private UUID productoId;
        @NotNull
        @Positive
        private Integer cantidad;
    }
}