package com.project.springboot.demoproject.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.BodegaRequest;
import com.project.springboot.demoproject.dto.BodegaResponse;
import com.project.springboot.demoproject.services.BodegaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bodegas")
@RequiredArgsConstructor
@Tag(name = "Bodegas", description = "CRUD de bodegas")
@SecurityRequirement(name = "bearerAuth")
public class BodegaController {

    private final BodegaService bodegaService;

    @GetMapping
    @Operation(summary = "Listar todas las bodegas (opcionalmente filtrar por ubicacion)")
    public List<BodegaResponse> listar(@RequestParam(required = false) String ubicacion) {
        return ubicacion == null ? bodegaService.listarTodas() : bodegaService.buscarPorUbicacion(ubicacion);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una bodega por id")
    public BodegaResponse obtener(@PathVariable Long id) {
        return bodegaService.obtenerPorId(id);
    }

    @PostMapping
    @Operation(summary = "Crear una bodega (solo ADMIN)")
    public ResponseEntity<BodegaResponse> crear(@Valid @RequestBody BodegaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bodegaService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una bodega (solo ADMIN)")
    public BodegaResponse actualizar(@PathVariable Long id, @Valid @RequestBody BodegaRequest request) {
        return bodegaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una bodega (solo ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        bodegaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
