package com.project.springboot.demoproject.dto.logitrackiq;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** POST /ordenes — el total se calcula SIEMPRE en el servidor (cantidad x precioUnitario). */
@Data
public class OrdenCompraRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;

    @NotNull(message = "La bodega destino es obligatoria")
    private Long bodegaDestinoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0", inclusive = true, message = "El precio unitario no puede ser negativo")
    private BigDecimal precioUnitario;
}
