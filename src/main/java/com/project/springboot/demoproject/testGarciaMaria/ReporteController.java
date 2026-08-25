package com.project.springboot.demoproject.testGarciaMaria;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.AuditoriaResponse;
import com.project.springboot.demoproject.dto.MovimientoResponse;
import com.project.springboot.demoproject.dto.reportes.ProductoMasMovidoDto;
import com.project.springboot.demoproject.dto.reportes.ReporteResumenDto;
import com.project.springboot.demoproject.dto.reportes.StockPorBodegaDto;
import com.project.springboot.demoproject.enums.TipoMovimiento;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Consultas avanzadas, resumen general y reportes filtrados de movimientos y auditoria")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;
   


    @GetMapping("/stock-por-bodega")
    @Operation(summary = "Stock total por bodega")
    public List<StockPorBodegaDto> stockPorBodega() {
        return reporteService.stockTotalPorBodega();
    }

    @GetMapping("/productos-mas-movidos")
    @Operation(summary = "Productos mas movidos (opcionalmente en un rango de fechas)")
    public List<ProductoMasMovidoDto> productosMasMovidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        if (desde != null && hasta != null) {
            return reporteService.productosMasMovidosPorFecha(desde, hasta);
        }
        return reporteService.productosMasMovidos();
    }

    @GetMapping("/resumen")
    @Operation(summary = "Resumen general del sistema: stock por bodega, productos mas movidos y totales")
    public ReporteResumenDto resumen() {
        return reporteService.resumenGeneral();
    }


    @GetMapping("/movimientos")
    @Operation(summary = "Movimientos filtrados por bodega (origen o destino), producto, tipo y/o rango de fechas")
    public List<MovimientoResponse> reporteMovimientos(
            @RequestParam(required = false) Long bodega,
            @RequestParam(required = false) Long producto,
            @RequestParam(required = false) TipoMovimiento tipoMovimiento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {

        return reporteService.reporteMovimientos(bodega, producto, tipoMovimiento, fechaInicio, fechaFin);
    }

    @GetMapping("/auditoria")
    @Operation(summary = "Auditoria filtrada por producto, rango de fecha de cambio y/o campo modificado")
    public List<AuditoriaResponse> reporteAuditoria(
            @RequestParam(required = false) Long producto,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) String campoModificado) {

        return reporteService.reporteAuditoria(producto, fechaInicio, fechaFin, campoModificado);
    }
}