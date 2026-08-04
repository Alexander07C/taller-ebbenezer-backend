package com.ebbenezer.taller.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ResumenMetodoResponse {

    private final String metodoPago;
    private final BigDecimal total;
    private final long cantidad;

    public ResumenMetodoResponse(String metodoPago, BigDecimal total, long cantidad) {
        this.metodoPago = metodoPago;
        this.total = total;
        this.cantidad = cantidad;
    }
}
