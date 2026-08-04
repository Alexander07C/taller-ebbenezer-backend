package com.ebbenezer.taller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class VentaRequest {

    private UUID clienteId;

    private String clienteNombre;

    private String descripcion;

    @NotNull
    private Boolean esFiado;

    private String metodoPago;

    @NotEmpty
    private List<ItemRequest> items;

    @Getter
    @Setter
    public static class ItemRequest {
        @NotNull
        private UUID productoId;
        private String productoNombre;
        @NotNull
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}
