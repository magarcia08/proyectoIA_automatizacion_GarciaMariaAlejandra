package com.project.springboot.demoproject.entities;

import com.project.springboot.demoproject.audit.Auditable;
import com.project.springboot.demoproject.audit.AuditoriaEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Proveedor de LogiTrack IQ. Se cargan con data.sql (ver docs/sdd/02-especificacion.md).
 * diasEntrega alimenta el punto de reorden de cada producto que lo tiene
 * como proveedor principal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proveedor")
@EntityListeners(AuditoriaEntityListener.class)
public class Proveedor implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String contacto;

    @Column(name = "dias_entrega", nullable = false)
    private Integer diasEntrega;

    @Override
    public String getNombreEntidad() {
        return "proveedor";
    }

    @Override
    public Long getEntidadId() {
        return id;
    }
}
