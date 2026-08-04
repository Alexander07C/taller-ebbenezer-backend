package com.ebbenezer.taller.dto;

import com.ebbenezer.taller.model.VentaItem;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class VentaItemResponse {

    private final UUID id;
    private final UUID productoId;
    private final String productoNombre;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public VentaItemResponse(VentaItem item) {
        this.id = item.getId();
        this.productoId = item.getProducto() != null ? item.getProducto().getId() : null;
        this.productoNombre = item.getProductoNombre() != null
                ? item.getProductoNombre()
                : (item.getProducto() != null ? item.getProducto().getNombre() : null);
        this.cantidad = item.getCantidad();
        this.precioUnitario = item.getPrecioUnitario();
        this.subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
    }
}
