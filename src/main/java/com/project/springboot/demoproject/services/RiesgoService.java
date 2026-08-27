package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.logitrackiq.CoberturaCalculo;
import com.project.springboot.demoproject.dto.logitrackiq.ProductoRiesgoResponse;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoDetalleRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Calcula consumo diario promedio, punto de reorden, dias de cobertura y la
 * lista de productos en riesgo (LogiTrack IQ). Ver reglas exactas en
 * docs/sdd/02-especificacion.md.
 *
 * FASE 2 (test: define reorder and order-state rules): solo la firma de los
 * metodos existe todavia, para que las pruebas escritas ANTES compilen y
 * fallen en rojo. La logica real se agrega en la fase
 * "feat: implement LogiTrack IQ rules".
 */
@Service
@RequiredArgsConstructor
public class RiesgoService {

    private final ProductoRepository productoRepository;
    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final MovimientoDetalleRepository movimientoDetalleRepository;

    /**
     * Calcula la cobertura de un producto sin importar si hoy aparece o no
     * en /productos/riesgo (se prueba por unidad de forma independiente).
     * Lanza ResourceNotFoundException si el producto no existe, y
     * BusinessException si no tiene proveedor principal.
     */
    @Transactional(readOnly = true)
    public CoberturaCalculo calcularCobertura(Long productoId) {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }

    /** GET /productos/riesgo: solo productos con proveedor principal y stockTotal < puntoReorden. */
    @Transactional(readOnly = true)
    public List<ProductoRiesgoResponse> listarProductosEnRiesgo() {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }
}
