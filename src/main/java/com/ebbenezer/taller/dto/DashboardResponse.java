package com.ebbenezer.taller.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class DashboardResponse {

    private final String periodo;
    private final BigDecimal totalVentas;
    private final long cantidadVentas;
    private final List<DashboardItem> detalle;

    public DashboardResponse(String periodo, BigDecimal totalVentas, long cantidadVentas, List<DashboardItem> detalle) {
        this.periodo = periodo;
        this.totalVentas = totalVentas;
        this.cantidadVentas = cantidadVentas;
        this.detalle = detalle;
    }

    @Getter
    public static class DashboardItem {
        private final String label;
        private final BigDecimal total;
        private final long cantidad;

        public DashboardItem(String label, BigDecimal total, long cantidad) {
            this.label = label;
            this.total = total;
            this.cantidad = cantidad;
        }
    }
}
