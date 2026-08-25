package com.project.springboot.demoproject.entities;

import java.time.LocalDateTime;

import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Registro de auditoria. Se crea automaticamente por AuditoriaEntityListener
 * cada vez que se inserta, actualiza o elimina una entidad marcada como
 * Auditable (Usuario, Bodega, Producto, InventarioBodega, Movimiento).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_operacion", nullable = false, columnDefinition = "tipo_operacion_auditoria")
    private TipoOperacionAuditoria tipoOperacion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "entidad_afectada", nullable = false, length = 100)
    private String entidadAfectada;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Lob
    @Column(name = "valores_anteriores")
    private String valoresAnteriores;

    @Lob
    @Column(name = "valores_nuevos")
    private String valoresNuevos;
}
