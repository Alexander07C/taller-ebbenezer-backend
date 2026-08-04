package com.ebbenezer.taller.repository;

import com.ebbenezer.taller.model.VentaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VentaItemRepository extends JpaRepository<VentaItem, UUID> {
    boolean existsByProductoId(UUID productoId);
}
