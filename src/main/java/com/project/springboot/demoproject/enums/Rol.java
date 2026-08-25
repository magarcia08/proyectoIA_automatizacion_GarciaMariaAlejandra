package com.project.springboot.demoproject.enums;

/**
 * Jerarquia de roles (ver SecurityConfig.roleHierarchy):
 *  - SUPERADMIN: hereda todos los permisos de ADMIN; unico rol que puede crear ADMIN.
 *  - ADMIN: gestiona bodegas/productos/inventario y puede crear usuarios EMPLEADO.
 *  - EMPLEADO: opera el dia a dia (movimientos, consultas), no gestiona usuarios.
 */
public enum Rol {
    SUPERADMIN,
    ADMIN,
    EMPLEADO
}
