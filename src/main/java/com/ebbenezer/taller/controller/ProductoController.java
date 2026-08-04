package com.ebbenezer.taller.controller;

import com.ebbenezer.taller.model.Producto;
import com.ebbenezer.taller.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public List<Producto> listar() {
        return productoService.listar();
    }

    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo() {
        return productoService.conStockBajo();
    }

    @GetMapping("/{id}")
    public Producto obtener(@PathVariable UUID id) {
        return productoService.obtener(id);
    }

    @PutMapping("/{id}")                                                                                                                               
     public Producto actualizar(@PathVariable UUID id, @Valid @RequestBody Producto producto) {                                                            
         return productoService.actualizar(id, producto); 
     }

    @PostMapping
    public Producto crear(@Valid @RequestBody Producto producto) {
        return productoService.crear(producto);
    }
    

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable UUID id) {
        productoService.eliminar(id);
    }

    @PostMapping("/{id}/entrada")
    public Producto registrarEntrada(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        int cantidad = (int) body.get("cantidad");
        String motivo = (String) body.getOrDefault("motivo", "Reabastecimiento");
        return productoService.registrarEntrada(id, cantidad, motivo);
    }
    
}
