package com.project.springboot.demoproject.dto;

import java.math.BigDecimal;

import com.project.springboot.demoproject.entities.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private Integer stockTotal; // suma del stock del producto en todas las bodegas

    public static ProductoResponse desde(Producto p, Integer stockTotal) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getCategoria(), p.getPrecio(), stockTotal);
    }
}
