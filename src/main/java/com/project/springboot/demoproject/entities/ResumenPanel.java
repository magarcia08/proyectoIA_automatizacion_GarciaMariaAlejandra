package com.project.springboot.demoproject.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.project.springboot.demoproject.audit.Auditable;
import com.project.springboot.demoproject.audit.AuditoriaEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resumen diario del panel (LogiTrack IQ). Solo puede existir uno por
 * fecha: publicar de nuevo para la misma fecha reemplaza contenidoJson
 * (ver PanelResumenService), y el reemplazo queda auditado porque esta
 * entidad implementa Auditable igual que el resto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resumen_panel")
@EntityListeners(AuditoriaEntityListener.class)
public class ResumenPanel implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Lob
    @Column(name = "contenido_json", nullable = false)
    private String contenidoJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Override
    public String getNombreEntidad() {
        return "resumen_panel";
    }

    @Override
    public Long getEntidadId() {
        return id;
    }
}
