package com.project.springboot.demoproject.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.InventarioBodegaRequest;
import com.project.springboot.demoproject.dto.InventarioBodegaResponse;
import com.project.springboot.demoproject.dto.reportes.StockBajoDto;
import com.project.springboot.demoproject.services.InventarioBodegaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Stock de productos por bodega")
@SecurityRequirement(name = "bearerAuth")
public class InventarioBodegaController {

    private final InventarioBodegaService inventarioBodegaService;

    @GetMapping
    @Operation(summary = "Listar inventario (opcionalmente filtrar por bodega)")
    public List<InventarioBodegaResponse> listar(@RequestParam(required = false) Long bodegaId) {
        return bodegaId == null ? inventarioBodegaService.listarTodo() : inventarioBodegaService.listarPorBodega(bodegaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un registro de inventario por id")
    public InventarioBodegaResponse obtener(@PathVariable Long id) {
        return inventarioBodegaService.obtenerPorId(id);
    }

    @GetMapping("/stock-bajo")
    @Operation(summary = "Productos con stock bajo (< 10 unidades)")
    public List<StockBajoDto> stockBajo() {
        return inventarioBodegaService.productosConStockBajo();
    }

    @PostMapping
    @Operation(summary = "Crear un registro de inventario (asignar producto a bodega) - solo ADMIN y SUPERADMIN")
    public ResponseEntity<InventarioBodegaResponse> crear(@Valid @RequestBody InventarioBodegaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioBodegaService.crear(request));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Ajustar manualmente el stock de un registro de inventario - solo ADMIN y SUPERADMIN")
    public InventarioBodegaResponse actualizarStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return inventarioBodegaService.actualizarStock(id, body.get("stock"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un registro de inventario - solo ADMIN")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        inventarioBodegaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
