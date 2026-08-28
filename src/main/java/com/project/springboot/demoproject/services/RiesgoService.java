package com.project.springboot.demoproject.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.logitrackiq.CoberturaCalculo;
import com.project.springboot.demoproject.dto.logitrackiq.ProductoRiesgoResponse;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.enums.EstadoCobertura;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoDetalleRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Calcula consumo diario promedio, punto de reorden, dias de cobertura y la
 * lista de productos en riesgo (LogiTrack IQ). Ver reglas exactas en
 * docs/sdd/02-especificacion.md.
 */
@Service
@RequiredArgsConstructor
public class RiesgoService {

    /** Zona horaria fija para "hoy", "ayer" y "ultimos 30 dias" (ver docs/sdd/02-especificacion.md). */
    public static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");

    private static final BigDecimal FACTOR_SEGURIDAD = new BigDecimal("1.5");
    private static final int ESCALA = 2;

    private final ProductoRepository productoRepository;
    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final MovimientoDetalleRepository movimientoDetalleRepository;

    @Transactional(readOnly = true)
    public CoberturaCalculo calcularCobertura(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> ResourceNotFoundException.of("Producto", productoId));
        if (producto.getProveedorPrincipal() == null) {
            throw new BusinessException(
                    "El producto '" + producto.getNombre() + "' no tiene proveedor principal: no se puede calcular su cobertura");
        }

        int stockTotal = stockTotal(productoId);
        BigDecimal consumoDiarioPromedio = consumoDiarioPromedio(productoId);

        int diasEntrega = producto.getProveedorPrincipal().getDiasEntrega();
        BigDecimal puntoReorden = consumoDiarioPromedio
                .multiply(BigDecimal.valueOf(diasEntrega))
                .multiply(FACTOR_SEGURIDAD)
                .setScale(ESCALA, RoundingMode.HALF_UP);

        BigDecimal diasCobertura = null;
        EstadoCobertura estadoCobertura;
        if (consumoDiarioPromedio.compareTo(BigDecimal.ZERO) == 0) {
            estadoCobertura = EstadoCobertura.SIN_CONSUMO;
        } else {
            diasCobertura = BigDecimal.valueOf(stockTotal).divide(consumoDiarioPromedio, ESCALA, RoundingMode.HALF_UP);
            estadoCobertura = EstadoCobertura.EN_RIESGO;
        }

        // Estrictamente menor: si el stock es IGUAL al punto de reorden, no esta en riesgo.
        boolean enRiesgo = BigDecimal.valueOf(stockTotal).compareTo(puntoReorden) < 0;

        return new CoberturaCalculo(stockTotal, consumoDiarioPromedio, puntoReorden, diasCobertura, estadoCobertura, enRiesgo);
    }

    /** GET /productos/riesgo: solo productos con proveedor principal y stockTotal < puntoReorden. */
    @Transactional(readOnly = true)
    public List<ProductoRiesgoResponse> listarProductosEnRiesgo() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getProveedorPrincipal() != null)
                .map(this::aRespuestaSiEstaEnRiesgo)
                .filter(Objects::nonNull)
                .toList();
    }

    private ProductoRiesgoResponse aRespuestaSiEstaEnRiesgo(Producto producto) {
        CoberturaCalculo calculo = calcularCobertura(producto.getId());
        if (!calculo.enRiesgo()) {
            return null;
        }
        ProductoRiesgoResponse respuesta = new ProductoRiesgoResponse();
        respuesta.setProductoId(producto.getId());
        respuesta.setNombreProducto(producto.getNombre());
        respuesta.setProveedorId(producto.getProveedorPrincipal().getId());
        respuesta.setStockTotal(calculo.stockTotal());
        respuesta.setConsumoDiarioPromedio(calculo.consumoDiarioPromedio());
        respuesta.setPuntoReorden(calculo.puntoReorden());
        respuesta.setDiasCobertura(calculo.diasCobertura());
        respuesta.setEstadoCobertura(calculo.estadoCobertura());
        respuesta.setBodegaDestinoId(sugerirBodegaDestino(producto.getId()));
        return respuesta;
    }

    /** Bodega con menor stock de ese producto; en empate, la de menor id. */
    private Long sugerirBodegaDestino(Long productoId) {
        return inventarioBodegaRepository.findByProductoId(productoId).stream()
                .min(Comparator.<com.project.springboot.demoproject.entities.InventarioBodega, Integer>comparing(ib -> ib.getStock())
                        .thenComparing(ib -> ib.getBodega().getId()))
                .map(ib -> ib.getBodega().getId())
                .orElse(null);
    }

    private BigDecimal consumoDiarioPromedio(Long productoId) {
        ZonedDateTime ahora = ZonedDateTime.now(ZONA_BOGOTA);
        // 30 dias calendario, incluida la fecha de consulta: [hoy-29 00:00, ahora]
        LocalDateTime inicio = ahora.toLocalDate().minusDays(29).atStartOfDay();
        LocalDateTime fin = ahora.toLocalDateTime();

        Integer totalSalidas = movimientoDetalleRepository.sumarCantidadPorProductoTipoYRango(
                productoId, TipoMovimiento.SALIDA, inicio, fin);
        if (totalSalidas == null) {
            totalSalidas = 0;
        }
        return BigDecimal.valueOf(totalSalidas).divide(BigDecimal.valueOf(30), ESCALA, RoundingMode.HALF_UP);
    }

    Integer stockTotal(Long productoId) {
        Integer total = inventarioBodegaRepository.obtenerStockTotalPorProducto(productoId);
        return total == null ? 0 : total;
    }
}
