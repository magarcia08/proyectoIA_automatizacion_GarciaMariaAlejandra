package com.project.springboot.demoproject.testGarciaMaria;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.AuditoriaResponse;
import com.project.springboot.demoproject.dto.MovimientoResponse;
import com.project.springboot.demoproject.dto.reportes.ProductoMasMovidoDto;
import com.project.springboot.demoproject.dto.reportes.ReporteResumenDto;
import com.project.springboot.demoproject.dto.reportes.StockPorBodegaDto;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.repositories.AuditoriaRepository;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoDetalleRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final MovimientoDetalleRepository movimientoDetalleRepository;
    private final BodegaRepository bodegaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final AuditoriaRepository auditoriaRepository;

    public List<StockPorBodegaDto> stockTotalPorBodega() {
        return inventarioBodegaRepository.obtenerStockTotalPorBodega().stream()
                .map(fila -> new StockPorBodegaDto((Long) fila[0], (String) fila[1], ((Number) fila[2]).longValue()))
                .toList();
    }

    public List<ProductoMasMovidoDto> productosMasMovidos() {
        return movimientoDetalleRepository.obtenerProductosMasMovidos().stream()
                .map(fila -> new ProductoMasMovidoDto((Long) fila[0], (String) fila[1], ((Number) fila[2]).longValue()))
                .toList();
    }

    public List<ProductoMasMovidoDto> productosMasMovidosPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoDetalleRepository.obtenerProductosMasMovidosPorFecha(inicio, fin).stream()
                .map(fila -> new ProductoMasMovidoDto((Long) fila[0], (String) fila[1], ((Number) fila[2]).longValue()))
                .toList();
    }

    /** Reporte general resumido en JSON, como pide el punto 6 del enunciado. */
    public ReporteResumenDto resumenGeneral() {
        ReporteResumenDto resumen = new ReporteResumenDto();
        resumen.setStockPorBodega(stockTotalPorBodega());
        resumen.setProductosMasMovidos(productosMasMovidos());
        resumen.setTotalBodegas(bodegaRepository.count());
        resumen.setTotalProductos(productoRepository.count());
        resumen.setTotalMovimientos(movimientoRepository.count());
        return resumen;
    }

    // ------------------------------------------------------------------
    // Modulo de reportes con filtros (examen): movimientos y auditoria
    // ------------------------------------------------------------------

    /**
     * Reporte de movimientos filtrado. Todos los parametros son opcionales;
     * si vienen en null simplemente no se aplican como filtro.
     */
    public List<MovimientoResponse> reporteMovimientos(Long bodegaId, Long productoId, TipoMovimiento tipoMovimiento,
            LocalDateTime fechaInicio, LocalDateTime fechaFin) {

        String tipoComoTexto = tipoMovimiento != null ? tipoMovimiento.name() : null;

        return movimientoRepository
                .buscarConFiltros(bodegaId, productoId, tipoComoTexto, fechaInicio, fechaFin)
                .stream()
                .map(MovimientoResponse::desde)
                .toList();
    }

    /**
     * Reporte de auditoria filtrado. Todos los parametros son opcionales;
     * si vienen en null simplemente no se aplican como filtro.
     */
    public List<AuditoriaResponse> reporteAuditoria(Long productoId, LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String campoModificado) {

        return auditoriaRepository
                .buscarConFiltros(productoId, fechaInicio, fechaFin, campoModificado)
                .stream()
                .map(AuditoriaResponse::desde)
                .toList();
    }
}