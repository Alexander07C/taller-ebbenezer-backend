package com.ebbenezer.taller.repository;

import com.ebbenezer.taller.model.EstadoVenta;
import com.ebbenezer.taller.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VentaRepository extends JpaRepository<Venta, UUID> {
    List<Venta> findByActivoTrue();
    List<Venta> findByClienteIdAndEstadoInAndActivoTrue(UUID clienteId, List<EstadoVenta> estados);
    List<Venta> findByEstadoAndActivoTrue(EstadoVenta estado);
    List<Venta> findAllByActivoTrueOrderByFechaDesc();
    List<Venta> findByClienteIdAndActivoTrueOrderByFechaDesc(UUID clienteId);
    List<Venta> findByEsFiadoTrueAndActivoTrueOrderByFechaDesc();
    List<Venta> findByEsFiadoFalseAndActivoTrueOrderByFechaDesc();
    List<Venta> findByFechaBetweenAndActivoTrueOrderByFechaAsc(LocalDateTime inicio, LocalDateTime fin);
}
