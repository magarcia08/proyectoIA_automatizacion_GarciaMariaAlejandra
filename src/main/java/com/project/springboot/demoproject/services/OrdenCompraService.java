package com.project.springboot.demoproject.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.audit.CurrentUserProvider;
import com.project.springboot.demoproject.dto.MovimientoDetalleRequest;
import com.project.springboot.demoproject.dto.MovimientoRequest;
import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraRequest;
import com.project.springboot.demoproject.dto.logitrackiq.OrdenCompraResponse;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.OrdenCompra;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Proveedor;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.EstadoOrden;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.exception.BusinessException;
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
 */
@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    /** Tabla de transiciones validas. Cualquier otra combinacion -> 400. */
    private static final Map<EstadoOrden, Set<EstadoOrden>> TRANSICIONES_VALIDAS = new EnumMap<>(Map.of(
            EstadoOrden.BORRADOR, EnumSet.of(EstadoOrden.APROBADA, EstadoOrden.CANCELADA),
            EstadoOrden.APROBADA, EnumSet.of(EstadoOrden.RECIBIDA, EstadoOrden.CANCELADA),
            EstadoOrden.RECIBIDA, EnumSet.noneOf(EstadoOrden.class),
            EstadoOrden.CANCELADA, EnumSet.noneOf(EstadoOrden.class)));

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final BodegaRepository bodegaRepository;
    private final MovimientoService movimientoService;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public OrdenCompraResponse crear(OrdenCompraRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> ResourceNotFoundException.of("Producto", request.getProductoId()));
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> ResourceNotFoundException.of("Proveedor", request.getProveedorId()));
        Bodega bodegaDestino = bodegaRepository.findById(request.getBodegaDestinoId())
                .orElseThrow(() -> ResourceNotFoundException.of("Bodega", request.getBodegaDestinoId()));
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new BusinessException("La cantidad de la orden debe ser mayor que 0");
        }
        Usuario usuarioActual = currentUserProvider.getUsuarioActual()
                .orElseThrow(() -> new BusinessException("No hay un usuario autenticado para crear la orden"));

        OrdenCompra orden = new OrdenCompra();
        orden.setProducto(producto);
        orden.setProveedor(proveedor);
        orden.setBodegaDestino(bodegaDestino);
        orden.setCantidad(request.getCantidad());
        orden.setPrecioUnitario(request.getPrecioUnitario());
        // El total SIEMPRE se calcula en el servidor, nunca se confia en el del cliente.
        orden.setTotal(request.getPrecioUnitario().multiply(BigDecimal.valueOf(request.getCantidad())));
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setEstado(EstadoOrden.BORRADOR);
        orden.setCreadoPor(usuarioActual);

        return OrdenCompraResponse.desde(ordenCompraRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listar(EstadoOrden estadoFiltro) {
        List<OrdenCompra> ordenes = estadoFiltro == null
                ? ordenCompraRepository.findAll()
                : ordenCompraRepository.findByEstado(estadoFiltro);
        return ordenes.stream().map(OrdenCompraResponse::desde).toList();
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
        EstadoOrden estadoDestino = parsearEstado(estadoDestinoTexto);
        OrdenCompra orden = buscarPorId(id);
        EstadoOrden estadoActual = orden.getEstado();

        Set<EstadoOrden> permitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoActual, EnumSet.noneOf(EstadoOrden.class));
        if (!permitidos.contains(estadoDestino)) {
            throw new BusinessException(
                    "No se puede cambiar la orden de " + estadoActual + " a " + estadoDestino + ". Transiciones permitidas desde "
                            + estadoActual + ": " + (permitidos.isEmpty() ? "ninguna" : permitidos));
        }

        if (estadoActual == EstadoOrden.APROBADA && estadoDestino == EstadoOrden.RECIBIDA) {
            registrarEntradaPorRecepcion(orden);
        }

        orden.setEstado(estadoDestino);
        // Cualquier cambio de estado invalida el PDF guardado: hay que generarlo de nuevo.
        orden.setPdfDocumento(null);
        orden.setPdfGeneradoEn(null);

        return OrdenCompraResponse.desde(ordenCompraRepository.save(orden));
    }

    private void registrarEntradaPorRecepcion(OrdenCompra orden) {
        MovimientoRequest movimiento = new MovimientoRequest();
        movimiento.setTipo(TipoMovimiento.ENTRADA);
        movimiento.setBodegaOrigenId(null);
        movimiento.setBodegaDestinoId(orden.getBodegaDestino().getId());

        MovimientoDetalleRequest detalle = new MovimientoDetalleRequest();
        detalle.setProductoId(orden.getProducto().getId());
        detalle.setCantidad(orden.getCantidad());
        movimiento.setDetalles(List.of(detalle));

        // Misma transaccion que el cambio de estado: si esto falla, todo se revierte.
        movimientoService.registrar(movimiento);
    }

    private EstadoOrden parsearEstado(String texto) {
        try {
            return EstadoOrden.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException("Estado invalido: '" + texto + "'. Valores permitidos: BORRADOR, APROBADA, RECIBIDA, CANCELADA");
        }
    }
}
