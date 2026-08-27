package com.project.springboot.demoproject.dto.logitrackiq;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.enums.EstadoOrden;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraResponse {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Long proveedorId;
    private String nombreProveedor;
    private Long bodegaDestinoId;
    private String nombreBodegaDestino;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal total;
    private LocalDateTime fechaCreacion;
    private EstadoOrden estado;
    private String creadoPor;
    private boolean pdfDisponible;
    private LocalDateTime pdfGeneradoEn;

    public static OrdenCompraResponse desde(OrdenCompra o) {
        OrdenCompraResponse r = new OrdenCompraResponse();
        r.setId(o.getId());
        r.setProductoId(o.getProducto().getId());
        r.setNombreProducto(o.getProducto().getNombre());
        r.setProveedorId(o.getProveedor().getId());
        r.setNombreProveedor(o.getProveedor().getNombre());
        r.setBodegaDestinoId(o.getBodegaDestino().getId());
        r.setNombreBodegaDestino(o.getBodegaDestino().getNombre());
        r.setCantidad(o.getCantidad());
        r.setPrecioUnitario(o.getPrecioUnitario());
        r.setTotal(o.getTotal());
        r.setFechaCreacion(o.getFechaCreacion());
        r.setEstado(o.getEstado());
        r.setCreadoPor(o.getCreadoPor().getUsername());
        r.setPdfDisponible(o.getPdfDocumento() != null);
        r.setPdfGeneradoEn(o.getPdfGeneradoEn());
        return r;
    }
}
