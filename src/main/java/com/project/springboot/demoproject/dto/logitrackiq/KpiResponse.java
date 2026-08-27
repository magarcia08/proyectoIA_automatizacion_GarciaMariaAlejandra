package com.project.springboot.demoproject.dto.logitrackiq;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Contrato exacto de GET /kpis (ver docs/sdd/02-especificacion.md). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiResponse {
    private OffsetDateTime calculadoEn;
    private List<OcupacionBodegaDto> ocupacionPorBodega;
    private Long productosEnQuiebre;
    private Long productosEnRiesgo;
    private OrdenesPorAprobarDto ordenesPorAprobar;
    private MovimientosAyerDto movimientosAyer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdenesPorAprobarDto {
        private Long cantidad;
        private BigDecimal montoTotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovimientosAyerDto {
        private Long entrada;
        private Long salida;
        private Long transferencia;
    }
}
