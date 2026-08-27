package com.project.springboot.demoproject.dto.logitrackiq;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** GET /panel/resumen — el contrato publicado + metadatos de auditoria. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenPanelResponse {
    private LocalDate fecha;
    private String narrativa;
    private List<AlertaDto> alertas;
    private List<AccionSugeridaDto> accionesSugeridas;
    private String autor;
    private LocalDateTime publicadoEn;
}
