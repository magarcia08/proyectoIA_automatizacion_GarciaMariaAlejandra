package com.project.springboot.demoproject.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.MovimientoRequest;
import com.project.springboot.demoproject.dto.MovimientoResponse;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.services.MovimientoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos", description = "Entradas, salidas y transferencias de inventario")
@SecurityRequirement(name = "bearerAuth")
public class MovimientoController {

    private final MovimientoService movimientoService;

    @GetMapping
    @Operation(summary = "Listar movimientos (filtros opcionales: rango de fechas, tipo o usuario)")
    public List<MovimientoResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long usuarioId) {

        if (desde != null && hasta != null) return movimientoService.buscarPorRangoFechas(desde, hasta);
        if (tipo != null) return movimientoService.buscarPorTipo(tipo);
        if (usuarioId != null) return movimientoService.buscarPorUsuario(usuarioId);
        return movimientoService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un movimiento por id, con su detalle de productos")
    public MovimientoResponse obtener(@PathVariable Long id) {
        return movimientoService.obtenerPorId(id);
    }

    @PostMapping
    @Operation(summary = "Registrar un movimiento (ENTRADA, SALIDA o TRANSFERENCIA)")
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.registrar(request));
    }
}
