package com.ebbenezer.taller.controller;

import com.ebbenezer.taller.dto.VentaResumenResponse;
import com.ebbenezer.taller.model.Cliente;
import com.ebbenezer.taller.repository.ClienteRepository;
import com.ebbenezer.taller.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final VentaService ventaService;

    @GetMapping
    public List<Cliente> listar() {
        return clienteRepository.findByActivoTrue();
    }

    @GetMapping("/buscar")
    public List<Cliente> buscar(@RequestParam String q) {
        return clienteRepository.findByNombreContainingIgnoreCaseAndActivoTrue(q);
    }

    @PostMapping
    public Cliente crear(@Valid @RequestBody Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @GetMapping("/{id}/deuda")
    public Map<String, Object> deudaPendiente(@PathVariable UUID id) {
        BigDecimal deuda = ventaService.calcularDeudaTotalCliente(id);
        return Map.of("clienteId", id, "deudaTotal", deuda);
    }

    @GetMapping("/{id}/ventas-pendientes")
    public List<VentaResumenResponse> ventasPendientes(@PathVariable UUID id) {
        return ventaService.listarPendientesDeCliente(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }
}
