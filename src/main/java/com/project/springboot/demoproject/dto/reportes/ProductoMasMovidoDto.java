package com.project.springboot.demoproject.dto.reportes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoMasMovidoDto {
    private Long productoId;
    private String productoNombre;
    private Long totalMovido;
}
