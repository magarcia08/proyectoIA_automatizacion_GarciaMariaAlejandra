package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.logitrackiq.KpiResponse;
import com.project.springboot.demoproject.dto.logitrackiq.OcupacionBodegaDto;
import com.project.springboot.demoproject.dto.logitrackiq.ProductoStockResponse;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

/**
 * GET /kpis, GET /bodegas/criticas y GET /productos/{id}/stock (LogiTrack IQ).
 * Ver docs/sdd/02-especificacion.md.
 *
 * FASE 2: solo la firma de los metodos, sin logica (ver RiesgoService).
 */
@Service
@RequiredArgsConstructor
public class KpiService {

    private final BodegaRepository bodegaRepository;
    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final RiesgoService riesgoService;

    @Transactional(readOnly = true)
    public KpiResponse calcularKpis() {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }

    /** Bodegas con ocupacion >= 90%. */
    @Transactional(readOnly = true)
    public List<OcupacionBodegaDto> listarBodegasCriticas() {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }

    @Transactional(readOnly = true)
    public ProductoStockResponse obtenerStock(Long productoId) {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }
}
