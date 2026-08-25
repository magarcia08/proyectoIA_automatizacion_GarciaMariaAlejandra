package com.project.springboot.demoproject.dto;

import java.util.List;

import com.project.springboot.demoproject.enums.TipoMovimiento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovimientoRequest {

    @NotNull(message = "El tipo de movimiento es obligatorio (ENTRADA, SALIDA, TRANSFERENCIA)")
    private TipoMovimiento tipo;

    // Requerido para SALIDA y TRANSFERENCIA
    private Long bodegaOrigenId;

    // Requerido para ENTRADA y TRANSFERENCIA
    private Long bodegaDestinoId;

    @NotEmpty(message = "Debe incluir al menos un producto en el movimiento")
    @Valid
    private List<MovimientoDetalleRequest> detalles;
}
