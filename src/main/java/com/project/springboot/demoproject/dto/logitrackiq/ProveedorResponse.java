package com.project.springboot.demoproject.dto.logitrackiq;

import com.project.springboot.demoproject.entities.Proveedor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorResponse {
    private Long id;
    private String nombre;
    private String contacto;
    private Integer diasEntrega;

    public static ProveedorResponse desde(Proveedor p) {
        return new ProveedorResponse(p.getId(), p.getNombre(), p.getContacto(), p.getDiasEntrega());
    }
}
