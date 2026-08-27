package com.project.springboot.demoproject.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.logitrackiq.KpiResponse;
import com.project.springboot.demoproject.services.KpiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/kpis")
@RequiredArgsConstructor
@Tag(name = "LogiTrack IQ - KPIs", description = "Indicadores del dashboard: ocupacion, quiebre, riesgo, ordenes por aprobar, movimientos de ayer")
@SecurityRequirement(name = "bearerAuth")
public class KpiController {

    private final KpiService kpiService;

    @GetMapping
    @Operation(summary = "Indicadores del dashboard (ver GET /kpis en docs/sdd/02-especificacion.md)")
    public KpiResponse obtenerKpis() {
        return kpiService.calcularKpis();
    }
}
