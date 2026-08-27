package com.project.springboot.demoproject.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.logitrackiq.ResumenPanelRequest;
import com.project.springboot.demoproject.dto.logitrackiq.ResumenPanelResponse;
import com.project.springboot.demoproject.services.PanelResumenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Resumen diario del panel (LogiTrack IQ). Ver docs/sdd/02-especificacion.md. */
@RestController
@RequestMapping("/panel/resumen")
@RequiredArgsConstructor
@Tag(name = "LogiTrack IQ - Resumen del panel")
@SecurityRequirement(name = "bearerAuth")
public class PanelResumenController {

    private final PanelResumenService panelResumenService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENTE', 'ADMIN', 'SUPERADMIN')")
    @Operation(summary = "Publicar/reemplazar el resumen del dia (AGENTE o ADMIN). JSON invalido -> 400, se conserva el ultimo valido.")
    public ResponseEntity<ResumenPanelResponse> publicar(@Valid @RequestBody ResumenPanelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(panelResumenService.publicar(request));
    }

    @GetMapping
    @Operation(summary = "Obtener el ultimo resumen valido (404 si no existe)")
    public ResumenPanelResponse obtenerUltimo() {
        return panelResumenService.obtenerUltimo();
    }
}
