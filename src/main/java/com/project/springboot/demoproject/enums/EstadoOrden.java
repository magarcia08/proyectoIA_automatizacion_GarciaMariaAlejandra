package com.project.springboot.demoproject.enums;

/**
 * Estados de una OrdenCompra (LogiTrack IQ). Transiciones validas:
 * BORRADOR -> APROBADA | CANCELADA
 * APROBADA -> RECIBIDA | CANCELADA
 * RECIBIDA -> (ninguno)
 * CANCELADA -> (ninguno)
 * Ver OrdenCompraService.cambiarEstado y docs/sdd/02-especificacion.md.
 */
public enum EstadoOrden {
    BORRADOR,
    APROBADA,
    RECIBIDA,
    CANCELADA
}
