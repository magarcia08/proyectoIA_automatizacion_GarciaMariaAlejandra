package com.project.springboot.demoproject.dto.logitrackiq;

import java.math.BigDecimal;

import com.project.springboot.demoproject.enums.EstadoCobertura;

/**
 * Resultado interno del calculo de cobertura de un producto (no es un DTO de
 * la API). Se usa tanto para construir GET /productos/riesgo como para
 * probar por unidad las reglas de consumo/reorden/cobertura, incluso para
 * productos que hoy no aparecerian en el listado publico (ver
 * RiesgoService.calcularCobertura y docs/sdd/02-especificacion.md).
 *
 * diasCobertura es null cuando consumoDiarioPromedio es 0 (estadoCobertura
 * queda en SIN_CONSUMO). enRiesgo es la fuente de verdad de si el producto
 * cuenta para /productos/riesgo y para el indicador productosEnRiesgo:
 * stockTotal < puntoReorden (estrictamente menor).
 */
public record CoberturaCalculo(
        Integer stockTotal,
        BigDecimal consumoDiarioPromedio,
        BigDecimal puntoReorden,
        BigDecimal diasCobertura,
        EstadoCobertura estadoCobertura,
        boolean enRiesgo) {
}
