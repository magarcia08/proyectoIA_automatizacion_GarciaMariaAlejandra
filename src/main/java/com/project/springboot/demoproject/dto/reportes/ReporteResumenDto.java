package com.project.springboot.demoproject.dto.reportes;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResumenDto {
    private List<StockPorBodegaDto> stockPorBodega;
    private List<ProductoMasMovidoDto> productosMasMovidos;
    private long totalBodegas;
    private long totalProductos;
    private long totalMovimientos;
}
