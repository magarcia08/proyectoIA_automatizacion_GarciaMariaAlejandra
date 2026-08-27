package com.project.springboot.demoproject.dto.logitrackiq;

import com.project.springboot.demoproject.enums.Severidad;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Una alerta dentro del contrato de POST /panel/resumen. Debe enlazar AL
 * MENOS uno de productoId/ordenId/bodegaId (validado en el servicio, ver
 * PanelResumenService — no es expresable solo con Bean Validation).
 */
@Data
public class AlertaDto {

    @NotNull(message = "La severidad es obligatoria (BAJA, MEDIA o ALTA)")
    private Severidad severidad;

    @NotBlank(message = "El titulo de la alerta es obligatorio")
    private String titulo;

    @NotBlank(message = "El detalle de la alerta es obligatorio")
    private String detalle;

    private Long productoId;
    private Long ordenId;
    private Long bodegaId;
}
