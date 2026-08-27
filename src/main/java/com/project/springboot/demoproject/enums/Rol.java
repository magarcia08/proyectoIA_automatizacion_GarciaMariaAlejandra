package com.project.springboot.demoproject.enums;

/**
 * Jerarquia de roles (ver SecurityConfig.roleHierarchy):
 *  - SUPERADMIN: hereda todos los permisos de ADMIN; unico rol que puede crear ADMIN.
 *  - ADMIN: gestiona bodegas/productos/inventario y puede crear usuarios EMPLEADO.
 *  - EMPLEADO: opera el dia a dia (movimientos, consultas), no gestiona usuarios.
 *  - AGENTE (LogiTrack IQ): usado por el servidor MCP / flujo n8n. Solo puede
 *    consultar KPIs/riesgo/bodegas criticas, crear ordenes en BORRADOR y
 *    publicar el resumen del panel. No puede aprobar, cancelar ni recibir
 *    ordenes, ni registrar movimientos manualmente (ver SecurityConfig).
 */
public enum Rol {
    SUPERADMIN,
    ADMIN,
    EMPLEADO,
    AGENTE
}
