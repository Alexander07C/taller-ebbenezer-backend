package com.ebbenezer.taller.repository;

import com.ebbenezer.taller.model.Abono;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AbonoRepository extends JpaRepository<Abono, UUID> {
}
