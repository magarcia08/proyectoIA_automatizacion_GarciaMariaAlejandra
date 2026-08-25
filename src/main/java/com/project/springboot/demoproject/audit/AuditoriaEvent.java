package com.project.springboot.demoproject.audit;

import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;

public record AuditoriaEvent(
        String username,
        String entidadAfectada,
        Long entidadId,
        TipoOperacionAuditoria tipoOperacion,
        String valoresAnteriores,
        String valoresNuevos
) {}