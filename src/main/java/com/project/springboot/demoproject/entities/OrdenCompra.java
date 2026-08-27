package com.project.springboot.demoproject.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.project.springboot.demoproject.audit.Auditable;
import com.project.springboot.demoproject.audit.AuditoriaEntityListener;
import com.project.springboot.demoproject.enums.EstadoOrden;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
 * Orden de compra de LogiTrack IQ. No es un PDF por si misma: el PDF
 * (con marca de agua BORRADOR mientras ese sea su estado) se genera y
 * guarda aparte via PdfService/POST /ordenes/{id}/pdf. Cualquier cambio
 * de estado borra el PDF guardado (ver OrdenCompraService).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orden_compra")
@EntityListeners(AuditoriaEntityListener.class)
public class OrdenCompra implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bodega_destino_id", nullable = false)
    private Bodega bodegaDestino;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_orden")
    private EstadoOrden estado = EstadoOrden.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_id", nullable = false)
    private Usuario creadoPor;

    @Lob
    @Column(name = "pdf_documento")
    private byte[] pdfDocumento;

    @Column(name = "pdf_generado_en")
    private LocalDateTime pdfGeneradoEn;

    @Override
    public String getNombreEntidad() {
        return "orden_compra";
    }

    @Override
    public Long getEntidadId() {
        return id;
    }
}
