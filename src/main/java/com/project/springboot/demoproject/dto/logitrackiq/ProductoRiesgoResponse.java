package com.project.springboot.demoproject.dto.logitrackiq;

import java.math.BigDecimal;

import com.project.springboot.demoproject.enums.EstadoCobertura;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Contrato de cada elemento de GET /productos/riesgo. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRiesgoResponse {
    private Long productoId;
    private String nombreProducto;
    private Long proveedorId;
    private Integer stockTotal;
    private BigDecimal consumoDiarioPromedio;
    private BigDecimal puntoReorden;
    private BigDecimal diasCobertura;
    private EstadoCobertura estadoCobertura;
    private Long bodegaDestinoId;
}
