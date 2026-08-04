package com.ebbenezer.taller.dto;

import com.ebbenezer.taller.model.Abono;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AbonoResponse {

    private final UUID id;
    private final BigDecimal monto;
    private final LocalDateTime fecha;

    public AbonoResponse(Abono abono) {
        this.id = abono.getId();
        this.monto = abono.getMonto();
        this.fecha = abono.getFecha();
    }
}
