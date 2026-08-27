package com.project.springboot.demoproject.enums;

/**
 * Estado de cobertura de un producto (LogiTrack IQ). SIN_CONSUMO cuando el
 * consumo diario promedio es 0 (dias de cobertura no se puede calcular,
 * se expone como null). EN_RIESGO en cualquier otro caso mostrado en
 * /productos/riesgo (esa lista ya filtra solo productos en riesgo).
 */
public enum EstadoCobertura {
    SIN_CONSUMO,
    EN_RIESGO
}
