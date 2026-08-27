package com.project.springboot.demoproject.dto.logitrackiq;

import com.project.springboot.demoproject.enums.TipoAccionResumen;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Una accion sugerida dentro del contrato de POST /panel/resumen. Debe
 * enlazar EXACTAMENTE uno de ordenId/productoId/bodegaId (validado en el
 * servicio, ver PanelResumenService).
 */
@Data
public class AccionSugeridaDto {

    @NotNull(message = "El tipo de accion es obligatorio (REVISAR_ORDEN, REVISAR_PRODUCTO o REVISAR_BODEGA)")
    private TipoAccionResumen tipo;

    @NotBlank(message = "La descripcion de la accion es obligatoria")
    private String descripcion;

    private Long ordenId;
    private Long productoId;
    private Long bodegaId;
}
