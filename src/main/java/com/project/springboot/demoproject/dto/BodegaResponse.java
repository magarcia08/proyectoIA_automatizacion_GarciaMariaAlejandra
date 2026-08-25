package com.project.springboot.demoproject.dto;

import com.project.springboot.demoproject.entities.Bodega;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodegaResponse {
    private Long id;
    private String nombre;
    private String ubicacion;
    private Integer capacidad;
    private String encargado;

    public static BodegaResponse desde(Bodega b) {
        return new BodegaResponse(b.getId(), b.getNombre(), b.getUbicacion(), b.getCapacidad(), b.getEncargado());
    }
}
