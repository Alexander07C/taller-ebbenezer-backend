package com.ebbenezer.taller.dto;

import com.ebbenezer.taller.model.EstadoVenta;
import com.ebbenezer.taller.model.Venta;
import com.ebbenezer.taller.model.VentaItem;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class VentaDetalleResponse {

    private final UUID id;
    private final Long numeroVenta;
    private final UUID clienteId;
    private final String clienteNombre;
    private final String descripcion;
    private final LocalDateTime fecha;
    private final List<VentaItemResponse> items;
    private final BigDecimal total;
    private final BigDecimal totalAbonado;
    private final String estado;
    private final boolean esFiado;
    private final BigDecimal saldoPendiente;
    private final int cantidadItems;
    private final String metodoPago;
    private final List<AbonoResponse> abonos;
    private final boolean esSalida;
    private final BigDecimal montoServicio;
    private final String concepto;
    private final String nombreCliente;
    private final String motivoAnulacion;

    public VentaDetalleResponse(Venta venta, BigDecimal saldoPendiente) {
        BigDecimal saldo = venta.getEstado() == EstadoVenta.ANULADO ? BigDecimal.ZERO : saldoPendiente;
        this.id = venta.getId();
        this.numeroVenta = venta.getNumeroVenta();
        this.clienteId = venta.getCliente() != null ? venta.getCliente().getId() : null;
        this.clienteNombre = venta.getCliente() != null
                ? venta.getCliente().getNombre()
                : venta.getClienteNombre();
        this.descripcion = venta.getDescripcion();
        this.fecha = venta.getFecha();
        this.items = venta.getItems().stream().map(VentaItemResponse::new).toList();
        this.total = venta.getTotal();
        this.totalAbonado = venta.getTotal().subtract(saldo);
        this.estado = venta.getEstado() == EstadoVenta.PAGADA ? "PAGADO" : venta.getEstado().name();
        this.esFiado = venta.isEsFiado();
        this.saldoPendiente = saldo;
        this.cantidadItems = venta.getItems().stream().mapToInt(VentaItem::getCantidad).sum();
        this.metodoPago = venta.getMetodoPago();
        this.abonos = venta.getAbonos().stream().map(AbonoResponse::new).toList();
        this.esSalida = venta.isEsSalida();
        this.montoServicio = venta.getMontoServicio() != null ? venta.getMontoServicio() : BigDecimal.ZERO;
        this.concepto = venta.getConcepto();
        this.nombreCliente = venta.getNombreCliente();
        this.motivoAnulacion = venta.getMotivoAnulacion();
    }
}
