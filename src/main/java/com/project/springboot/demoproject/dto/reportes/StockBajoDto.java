package com.project.springboot.demoproject.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockBajoDto {
    private Long bodegaId;
    private String bodegaNombre;
    private Long productoId;
    private String productoNombre;
    private Integer stock;
}
