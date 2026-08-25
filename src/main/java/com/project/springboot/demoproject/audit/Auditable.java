package com.project.springboot.demoproject.audit;

/**
 * Contrato que deben implementar las entidades que quieren ser registradas
 * automaticamente en la tabla "auditoria" por AuditoriaEntityListener.
 */
public interface Auditable {

    /** Nombre logico de la entidad, tal como se guarda en auditoria.entidad_afectada */
    String getNombreEntidad();

    /** Id de la fila afectada (puede ser null antes del primer INSERT) */
    Long getEntidadId();
}
