package com.ebbenezer.taller.controller;

import com.ebbenezer.taller.dto.AnularSalidaRequest;
import com.ebbenezer.taller.dto.SalidaRequest;
import com.ebbenezer.taller.dto.VentaDetalleResponse;
import com.ebbenezer.taller.service.SalidaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/salidas")
@RequiredArgsConstructor
public class SalidaController {

    private final SalidaService salidaService;

    @PostMapping
    public VentaDetalleResponse registrar(@Valid @RequestBody SalidaRequest request) {
        return salidaService.registrarSalida(request);
    }

    @PatchMapping("/{id}/anulada")
    public VentaDetalleResponse anular(@PathVariable UUID id, @Valid @RequestBody AnularSalidaRequest request) {
        return salidaService.anularSalida(id, request);
    }
}