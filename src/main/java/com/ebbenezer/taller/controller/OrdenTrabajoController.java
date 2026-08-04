package com.ebbenezer.taller.controller;

import com.ebbenezer.taller.model.OrdenTrabajo;
import com.ebbenezer.taller.repository.OrdenTrabajoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordenes-trabajo")
@RequiredArgsConstructor
public class OrdenTrabajoController {

    private final OrdenTrabajoRepository ordenTrabajoRepository;

    @GetMapping
    public List<OrdenTrabajo> listar() {
        return ordenTrabajoRepository.findAll();
    }

    @PostMapping
    public OrdenTrabajo crear(@Valid @RequestBody OrdenTrabajo orden) {
        return ordenTrabajoRepository.save(orden);
    }

    @PatchMapping("/{id}/estado")
    public OrdenTrabajo cambiarEstado(@PathVariable UUID id, @RequestParam OrdenTrabajo.Estado estado) {
        OrdenTrabajo orden = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        orden.setEstado(estado);
        return ordenTrabajoRepository.save(orden);
    }
}
