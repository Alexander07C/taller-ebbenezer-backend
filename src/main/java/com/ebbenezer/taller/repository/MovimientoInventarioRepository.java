package com.ebbenezer.taller.repository;

import com.ebbenezer.taller.model.MovimientoInventario;
import com.ebbenezer.taller.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, UUID> {
    void deleteByProducto(Producto producto);
}
