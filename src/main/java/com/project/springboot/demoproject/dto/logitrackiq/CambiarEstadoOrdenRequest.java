package com.project.springboot.demoproject.dto.logitrackiq;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** PATCH /ordenes/{id}/estado — body exacto: {"estado": "APROBADA"}. */
@Data
public class CambiarEstadoOrdenRequest {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
