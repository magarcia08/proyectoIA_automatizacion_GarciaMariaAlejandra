package com.project.springboot.demoproject.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.AuditoriaResponse;
import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;
import com.project.springboot.demoproject.services.AuditoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auditorias")
@RequiredArgsConstructor
@Tag(name = "Auditoria", description = "Consulta de cambios registrados automaticamente (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    @Operation(summary = "Listar auditorias (filtros opcionales: usuario, tipo de operacion, entidad o rango de fechas)")
    public List<AuditoriaResponse> listar(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) TipoOperacionAuditoria tipoOperacion,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        if (usuarioId != null) return auditoriaService.buscarPorUsuario(usuarioId);
        if (tipoOperacion != null) return auditoriaService.buscarPorTipoOperacion(tipoOperacion);
        if (entidad != null) return auditoriaService.buscarPorEntidad(entidad);
        if (desde != null && hasta != null) return auditoriaService.buscarPorRangoFechas(desde, hasta);
        return auditoriaService.listarTodas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un registro de auditoria por id")
    public AuditoriaResponse obtener(@PathVariable Long id) {
        return auditoriaService.obtenerPorId(id);
    }
}
