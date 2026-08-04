package com.ebbenezer.taller.controller;

import com.ebbenezer.taller.dto.AbonoRequest;
import com.ebbenezer.taller.dto.VentaDetalleResponse;
import com.ebbenezer.taller.dto.VentaRequest;
import com.ebbenezer.taller.dto.VentaResumenResponse;
import com.ebbenezer.taller.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public List<VentaResumenResponse> listar(@RequestParam(required = false) UUID clienteId) {
        if (clienteId != null) {
            return ventaService.listarPorCliente(clienteId);
        }
        return ventaService.listarTodas();
    }

    @GetMapping("/diarias")
    public List<VentaResumenResponse> diarias() {
        return ventaService.listarDiarias();
    }

    @GetMapping("/fiadas")
    public List<VentaResumenResponse> fiadas() {
        return ventaService.listarFiadas();
    }

    @GetMapping("/pendientes")
    public List<VentaResumenResponse> pendientes() {
        return ventaService.listarPendientes();
    }

    @PostMapping
    public VentaDetalleResponse registrar(@Valid @RequestBody VentaRequest request) {
        return ventaService.registrarVenta(request);
    }

    @GetMapping("/{id}")
    public VentaDetalleResponse detalle(@PathVariable UUID id) {
        return ventaService.obtenerDetalle(id);
    }

    @PostMapping("/abonos")
    public VentaDetalleResponse registrarAbono(@Valid @RequestBody AbonoRequest request) {
        return ventaService.registrarAbono(request);
    }

    @PostMapping("/{id}/abonos")
    public VentaDetalleResponse registrarAbono(@PathVariable UUID id,
                                               @Valid @RequestBody AbonoRequest request) {
        request.setVentaId(id);
        return ventaService.registrarAbono(request);
    }

    @PostMapping("/{id}/pagar")
    public VentaDetalleResponse pagarCompleto(@PathVariable UUID id) {
        return ventaService.pagarCompleto(id);
    }

    @PostMapping("/{id}/anular")
    public VentaDetalleResponse anular(@PathVariable UUID id) {
        return ventaService.anularVenta(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable UUID id) {
        ventaService.eliminarVenta(id);
    }
}
