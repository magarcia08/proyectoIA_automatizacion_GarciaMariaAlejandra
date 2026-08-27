package com.project.springboot.demoproject.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.logitrackiq.CambiarEstadoOrdenRequest;
import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraRequest;
import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraResponse;
import com.project.springboot.demoproject.enums.EstadoOrden;
import com.project.springboot.demoproject.services.OrdenCompraService;
import com.project.springboot.demoproject.services.PdfService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Ordenes de compra de LogiTrack IQ (ver docs/sdd/02-especificacion.md).
 * AGENTE y ADMIN pueden crear ordenes en BORRADOR; solo ADMIN/SUPERADMIN
 * pueden cambiar el estado o generar/ver el PDF.
 */
@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@Tag(name = "LogiTrack IQ - Ordenes de compra")
@SecurityRequirement(name = "bearerAuth")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;
    private final PdfService pdfService;

    @GetMapping
    @Operation(summary = "Listar ordenes (filtro opcional por estado)")
    public List<OrdenCompraResponse> listar(@RequestParam(required = false) EstadoOrden estado) {
        return ordenCompraService.listar(estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una orden por id")
    public OrdenCompraResponse obtener(@PathVariable Long id) {
        return ordenCompraService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENTE', 'ADMIN', 'SUPERADMIN')")
    @Operation(summary = "Crear una orden en BORRADOR (AGENTE o ADMIN). El total se calcula en el servidor.")
    public ResponseEntity<OrdenCompraResponse> crear(@Valid @RequestBody OrdenCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCompraService.crear(request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "Cambiar el estado de una orden (solo ADMIN/SUPERADMIN). AGENTE recibe 403.")
    public OrdenCompraResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoOrdenRequest request) {
        return ordenCompraService.cambiarEstado(id, request.getEstado());
    }

    @PostMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "Generar y guardar el PDF de la orden (marca de agua BORRADOR si aplica). Reemplaza el anterior si ya existia.")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long id) {
        byte[] pdf = pdfService.generarYGuardar(id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "Descargar/visualizar el PDF guardado de la orden. 404 si aun no se ha generado.")
    public ResponseEntity<byte[]> obtenerPdf(@PathVariable Long id) {
        byte[] pdf = pdfService.obtenerPdfGuardado(id);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf);
    }
}
