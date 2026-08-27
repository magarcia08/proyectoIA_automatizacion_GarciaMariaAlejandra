package com.project.springboot.demoproject.dto.logitrackiq;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Contrato ESTRICTO de POST /panel/resumen: no admite propiedades
 * adicionales (el ObjectMapper por defecto de Spring Boot ya falla con
 * FAIL_ON_UNKNOWN_PROPERTIES, no hace falta configuracion extra).
 */
@Data
public class ResumenPanelRequest {

    @NotNull(message = "La fecha es obligatoria (YYYY-MM-DD)")
    private LocalDate fecha;

    @NotNull(message = "La narrativa es obligatoria")
    @Size(min = 20, max = 500, message = "La narrativa debe tener entre 20 y 500 caracteres")
    private String narrativa;

    @NotNull(message = "alertas es obligatorio (puede ser un arreglo vacio)")
    @Valid
    private List<AlertaDto> alertas;

    @NotNull(message = "accionesSugeridas es obligatorio (puede ser un arreglo vacio)")
    @Valid
    private List<AccionSugeridaDto> accionesSugeridas;
}
