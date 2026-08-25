package com.project.springboot.demoproject.dto;

import com.project.springboot.demoproject.entities.InventarioBodega;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioBodegaResponse {
    private Long id;
    private Long bodegaId;
    private String bodegaNombre;
    private Long productoId;
    private String productoNombre;
    private Integer stock;

    public static InventarioBodegaResponse desde(InventarioBodega ib) {
        return new InventarioBodegaResponse(
                ib.getId(),
                ib.getBodega().getId(), ib.getBodega().getNombre(),
                ib.getProducto().getId(), ib.getProducto().getNombre(),
                ib.getStock());
    }
}
