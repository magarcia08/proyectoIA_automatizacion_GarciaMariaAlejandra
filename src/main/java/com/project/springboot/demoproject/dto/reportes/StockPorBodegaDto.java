package com.project.springboot.demoproject.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPorBodegaDto {
    private Long bodegaId;
    private String bodegaNombre;
    private Long stockTotal;
}
