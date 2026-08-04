package com.ebbenezer.taller.repository;

import com.ebbenezer.taller.model.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, UUID> {
}
