package com.ebbenezer.taller.service;

import com.ebbenezer.taller.model.MovimientoInventario;
import com.ebbenezer.taller.model.Producto;
import com.ebbenezer.taller.repository.MovimientoInventarioRepository;
import com.ebbenezer.taller.repository.ProductoRepository;
import com.ebbenezer.taller.repository.VentaItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final VentaItemRepository ventaItemRepository;

    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    public Producto crear(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(UUID id, Producto producto) {
        Producto existente = obtener(id);
        if (producto.getNombre() != null) existente.setNombre(producto.getNombre());
        if (producto.getSku() != null) existente.setSku(producto.getSku());
        if (producto.getStockActual() != null) existente.setStockActual(producto.getStockActual());
        if (producto.getStockMinimo() != null) existente.setStockMinimo(producto.getStockMinimo());
        if (producto.getPrecio() != null) existente.setPrecio(producto.getPrecio());
        if (producto.getCategoria() != null) existente.setCategoria(producto.getCategoria());
        return productoRepository.save(existente);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (ventaItemRepository.existsByProductoId(id)) {
            throw new IllegalStateException("No se puede eliminar el producto porque tiene ventas asociadas");
        }
        Producto producto = obtener(id);
        movimientoInventarioRepository.deleteByProducto(producto);
        productoRepository.delete(producto);
    }

    public Producto obtener(UUID id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    public List<Producto> conStockBajo() {
        // Trae productos cuyo stock actual ya llego (o esta por debajo) del minimo definido
        return productoRepository.findAll().stream()
                .filter(p -> p.getStockActual() <= p.getStockMinimo())
                .toList();
    }

    public Producto registrarEntrada(UUID productoId, int cantidad, String motivo) {
        Producto producto = obtener(productoId);
        producto.setStockActual(producto.getStockActual() + cantidad);
        productoRepository.save(producto);
        guardarMovimiento(producto, MovimientoInventario.Tipo.ENTRADA, cantidad, motivo);
        return producto;
    }

    public void descontarStock(Producto producto, int cantidad, String motivo) {
        if (producto.getStockActual() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para " + producto.getNombre());
        }
        producto.setStockActual(producto.getStockActual() - cantidad);
        productoRepository.save(producto);
        guardarMovimiento(producto, MovimientoInventario.Tipo.SALIDA, cantidad, motivo);
    }

    private void guardarMovimiento(Producto producto, MovimientoInventario.Tipo tipo, int cantidad, String motivo) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setMotivo(motivo);
        movimientoInventarioRepository.save(mov);
    }
}
