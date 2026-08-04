package com.ebbenezer.taller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class AbonoRequest {

    @NotNull
    private UUID ventaId;

    @NotNull
    @Positive
    private BigDecimal monto;
}
