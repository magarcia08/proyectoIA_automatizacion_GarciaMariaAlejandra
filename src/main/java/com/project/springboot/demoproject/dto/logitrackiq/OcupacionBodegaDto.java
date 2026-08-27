package com.project.springboot.demoproject.dto.logitrackiq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Ocupacion de una bodega: (stock almacenado / capacidad) x 100. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcupacionBodegaDto {
    private Long bodegaId;
    private String nombre;
    private Double porcentaje;
}
