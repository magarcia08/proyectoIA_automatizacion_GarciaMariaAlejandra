package com.project.springboot.demoproject.dto;

import java.time.LocalDateTime;

import com.project.springboot.demoproject.entities.Auditoria;
import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaResponse {
    private Long id;
    private TipoOperacionAuditoria tipoOperacion;
    private LocalDateTime fechaHora;
    private Long usuarioId;
    private String usuarioUsername;
    private String entidadAfectada;
    private Long entidadId;
    private String valoresAnteriores;
    private String valoresNuevos;

    public static AuditoriaResponse desde(Auditoria a) {
        return new AuditoriaResponse(a.getId(), a.getTipoOperacion(), a.getFechaHora(),
                a.getUsuario().getId(), a.getUsuario().getUsername(),
                a.getEntidadAfectada(), a.getEntidadId(),
                a.getValoresAnteriores(), a.getValoresNuevos());
    }
}
