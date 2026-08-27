package com.project.springboot.demoproject.dto.logitrackiq;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Contrato de GET /productos/{id}/stock: total y desglose por bodega. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoStockResponse {
    private Long productoId;
    private String nombreProducto;
    private Integer stockTotal;
    private List<StockPorBodegaDto> porBodega;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockPorBodegaDto {
        private Long bodegaId;
        private String nombreBodega;
        private Integer stock;
    }
}
