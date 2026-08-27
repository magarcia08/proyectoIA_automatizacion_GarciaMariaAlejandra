package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.audit.CurrentUserProvider;
import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraRequest;
import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraResponse;
import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.enums.EstadoOrden;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;
import com.project.springboot.demoproject.repositories.ProveedorRepository;

import lombok.RequiredArgsConstructor;

/**
 * Crea ordenes en BORRADOR y aplica la maquina de estados (LogiTrack IQ).
 * La transicion APROBADA -> RECIBIDA crea un movimiento ENTRADA en una
 * sola transaccion (reutiliza MovimientoService). Ver
 * docs/sdd/02-especificacion.md.
 *
 * FASE 2: solo la firma de los metodos, sin logica (ver RiesgoService).
 */
@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final BodegaRepository bodegaRepository;
    private final MovimientoService movimientoService;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public OrdenCompraResponse crear(OrdenCompraRequest request) {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }

    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listar(EstadoOrden estadoFiltro) {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }

    @Transactional(readOnly = true)
    public OrdenCompraResponse obtener(Long id) {
        return OrdenCompraResponse.desde(buscarPorId(id));
    }

    public OrdenCompra buscarPorId(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("OrdenCompra", id));
    }

    /**
     * Cambia el estado siguiendo la tabla de transiciones. En
     * APROBADA -> RECIBIDA crea el movimiento ENTRADA correspondiente en la
     * MISMA transaccion (todo o nada). Cualquier cambio de estado borra el
     * PDF guardado. Solo ADMIN/SUPERADMIN pueden llamar este metodo
     * (aplicado con @PreAuthorize en el controlador).
     */
    @Transactional
    public OrdenCompraResponse cambiarEstado(Long id, String estadoDestinoTexto) {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }
}
