package com.project.springboot.demoproject.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.springboot.demoproject.entities.Bodega;

public interface BodegaRepository extends JpaRepository<Bodega, Long> {

    Optional<Bodega> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Bodega> findByUbicacionContainingIgnoreCase(String ubicacion);

    List<Bodega> findByEncargadoContainingIgnoreCase(String encargado);
}
