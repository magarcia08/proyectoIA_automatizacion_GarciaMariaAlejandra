package com.project.springboot.demoproject.dto;

import com.project.springboot.demoproject.entities.MovimientoDetalle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoDetalleResponse {
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;

    public static MovimientoDetalleResponse desde(MovimientoDetalle md) {
        return new MovimientoDetalleResponse(md.getProducto().getId(), md.getProducto().getNombre(), md.getCantidad());
    }
}
