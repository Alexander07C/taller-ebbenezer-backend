package com.ebbenezer.taller.repository;

import com.ebbenezer.taller.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    List<Producto> findByStockActualLessThanEqual(Integer stockMinimo);
}
