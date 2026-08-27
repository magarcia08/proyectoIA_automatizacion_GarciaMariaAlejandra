package com.project.springboot.demoproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.springboot.demoproject.entities.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
}
