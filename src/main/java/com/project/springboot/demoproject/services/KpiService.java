package com.project.springboot.demoproject.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.logitrackiq.KpiResponse;
import com.project.springboot.demoproject.dto.logitrackiq.OcupacionBodegaDto;
import com.project.springboot.demoproject.dto.logitrackiq.ProductoStockResponse;
import com.project.springboot.demoproject.entities.InventarioBodega;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.enums.EstadoOrden;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

/**
 * GET /kpis, GET /bodegas/criticas y GET /productos/{id}/stock (LogiTrack IQ).
 * Ver docs/sdd/02-especificacion.md.
 */
@Service
@RequiredArgsConstructor
public class KpiService {

    private static final double UMBRAL_CRITICO = 90.0;

    private final BodegaRepository bodegaRepository;
    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final RiesgoService riesgoService;

    @Transactional(readOnly = true)
    public KpiResponse calcularKpis() {
        KpiResponse respuesta = new KpiResponse();
        respuesta.setCalculadoEn(OffsetDateTime.now(RiesgoService.ZONA_BOGOTA));
        respuesta.setOcupacionPorBodega(calcularOcupacionPorBodega());
        respuesta.setProductosEnQuiebre(contarProductosEnQuiebre());
        respuesta.setProductosEnRiesgo((long) riesgoService.listarProductosEnRiesgo().size());
        respuesta.setOrdenesPorAprobar(calcularOrdenesPorAprobar());
        respuesta.setMovimientosAyer(calcularMovimientosAyer());
        return respuesta;
    }

    /** Bodegas con ocupacion >= 90%. */
    @Transactional(readOnly = true)
    public List<OcupacionBodegaDto> listarBodegasCriticas() {
        return calcularOcupacionPorBodega().stream()
                .filter(o -> o.getPorcentaje() >= UMBRAL_CRITICO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoStockResponse obtenerStock(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> ResourceNotFoundException.of("Producto", productoId));

        List<InventarioBodega> filas = inventarioBodegaRepository.findByProductoId(productoId);
        List<ProductoStockResponse.StockPorBodegaDto> porBodega = filas.stream()
                .map(ib -> new ProductoStockResponse.StockPorBodegaDto(
                        ib.getBodega().getId(), ib.getBodega().getNombre(), ib.getStock()))
                .toList();
        int total = filas.stream().mapToInt(InventarioBodega::getStock).sum();

        return new ProductoStockResponse(productoId, producto.getNombre(), total, porBodega);
    }

    // ------------------------------------------------------------------

    private List<OcupacionBodegaDto> calcularOcupacionPorBodega() {
        Map<Long, Integer> stockPorBodega = inventarioBodegaRepository.obtenerStockTotalPorBodega().stream()
                .collect(Collectors.toMap(fila -> (Long) fila[0], fila -> ((Number) fila[2]).intValue()));

        return bodegaRepository.findAll().stream()
                .map(b -> {
                    int stock = stockPorBodega.getOrDefault(b.getId(), 0);
                    double porcentaje = redondear((stock * 100.0) / b.getCapacidad());
                    return new OcupacionBodegaDto(b.getId(), b.getNombre(), porcentaje);
                })
                .toList();
    }

    private long contarProductosEnQuiebre() {
        return productoRepository.findAll().stream()
                .filter(p -> stockTotalProducto(p.getId()) == 0)
                .count();
    }

    private int stockTotalProducto(Long productoId) {
        Integer total = inventarioBodegaRepository.obtenerStockTotalPorProducto(productoId);
        return total == null ? 0 : total;
    }

    private KpiResponse.OrdenesPorAprobarDto calcularOrdenesPorAprobar() {
        List<com.project.springboot.demoproject.entities.OrdenCompra> borradores = ordenCompraRepository.findByEstado(EstadoOrden.BORRADOR);
        BigDecimal montoTotal = borradores.stream()
                .map(com.project.springboot.demoproject.entities.OrdenCompra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new KpiResponse.OrdenesPorAprobarDto((long) borradores.size(), montoTotal);
    }

    private KpiResponse.MovimientosAyerDto calcularMovimientosAyer() {
        ZonedDateTime ahora = ZonedDateTime.now(RiesgoService.ZONA_BOGOTA);
        LocalDate ayer = ahora.toLocalDate().minusDays(1);
        LocalDateTime inicio = ayer.atStartOfDay();
        LocalDateTime fin = ayer.atTime(LocalTime.MAX);

        long entrada = movimientoRepository.countByTipoAndFechaBetween(TipoMovimiento.ENTRADA, inicio, fin);
        long salida = movimientoRepository.countByTipoAndFechaBetween(TipoMovimiento.SALIDA, inicio, fin);
        long transferencia = movimientoRepository.countByTipoAndFechaBetween(TipoMovimiento.TRANSFERENCIA, inicio, fin);

        return new KpiResponse.MovimientosAyerDto(entrada, salida, transferencia);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
