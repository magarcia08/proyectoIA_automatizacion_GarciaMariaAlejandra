package com.project.springboot.demoproject.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.audit.CurrentUserProvider;
import com.project.springboot.demoproject.dto.MovimientoDetalleRequest;
import com.project.springboot.demoproject.dto.MovimientoRequest;
import com.project.springboot.demoproject.dto.MovimientoResponse;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.InventarioBodega;
import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.entities.MovimientoDetalle;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.TipoMovimiento;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.MovimientoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Contiene la logica de negocio de los movimientos de inventario:
 * valida las bodegas segun el tipo de movimiento, controla el stock
 * disponible y actualiza inventario_bodega de forma coherente.
 */
@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final BodegaService bodegaService;
    private final ProductoService productoService;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request) {
        validarBodegasSegunTipo(request);

        Usuario usuarioActual = currentUserProvider.getUsuarioActual()
                .orElseThrow(() -> new BusinessException("No hay un usuario autenticado para registrar el movimiento"));

        Bodega origen = request.getBodegaOrigenId() != null ? bodegaService.buscarPorId(request.getBodegaOrigenId()) : null;
        Bodega destino = request.getBodegaDestinoId() != null ? bodegaService.buscarPorId(request.getBodegaDestinoId()) : null;

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setTipo(request.getTipo());
        movimiento.setUsuario(usuarioActual);
        movimiento.setBodegaOrigen(origen);
        movimiento.setBodegaDestino(destino);

        // IMPORTANTE: se guarda el Movimiento PRIMERO, sin detalles, para que
        // Hibernate ejecute el INSERT y obtenga el id real (GenerationType.IDENTITY).
        // Si se agregan los detalles a la coleccion ANTES de este save, Hibernate no
        // puede resolver el orden del cascade hacia MovimientoDetalle (FK no nula)
        // porque el id del padre todavia no existe en ese momento. Eso es lo que
        // produce el error "null id ... don't flush the Session after an exception
        // occurs" / AssertionFailure de Hibernate.
        Movimiento guardado = movimientoRepository.save(movimiento);

        for (MovimientoDetalleRequest detalleReq : request.getDetalles()) {
            Producto producto = productoService.buscarPorId(detalleReq.getProductoId());
            aplicarEfectoEnInventario(request.getTipo(), origen, destino, producto, detalleReq.getCantidad());

            MovimientoDetalle detalle = new MovimientoDetalle();
            detalle.setMovimiento(guardado);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleReq.getCantidad());
            guardado.getDetalles().add(detalle);
        }

        // Ahora que "guardado" ya tiene id, el cascade = ALL persiste los detalles
        // sin ambiguedad de orden para Hibernate.
        movimientoRepository.save(guardado);

        return MovimientoResponse.desde(guardado);
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listarTodos() {
        return movimientoRepository.findAllConDetalles().stream().map(MovimientoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public MovimientoResponse obtenerPorId(Long id) {
        return MovimientoResponse.desde(buscarPorId(id));
    }

    public Movimiento buscarPorId(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Movimiento", id));
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByFechaBetween(inicio, fin).stream().map(MovimientoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> buscarPorTipo(TipoMovimiento tipo) {
        return movimientoRepository.findByTipo(tipo).stream().map(MovimientoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> buscarPorUsuario(Long usuarioId) {
        return movimientoRepository.findByUsuarioId(usuarioId).stream().map(MovimientoResponse::desde).toList();
    }

    // ------------------------------------------------------------------
    // Reglas de negocio
    // ------------------------------------------------------------------

    private void validarBodegasSegunTipo(MovimientoRequest request) {
        TipoMovimiento tipo = request.getTipo();
        Long origenId = request.getBodegaOrigenId();
        Long destinoId = request.getBodegaDestinoId();

        switch (tipo) {
            case ENTRADA -> {
                if (destinoId == null || origenId != null) {
                    throw new BusinessException("Una ENTRADA requiere solo bodega destino (sin bodega origen)");
                }
            }
            case SALIDA -> {
                if (origenId == null || destinoId != null) {
                    throw new BusinessException("Una SALIDA requiere solo bodega origen (sin bodega destino)");
                }
            }
            case TRANSFERENCIA -> {
                if (origenId == null || destinoId == null) {
                    throw new BusinessException("Una TRANSFERENCIA requiere bodega origen y bodega destino");
                }
                if (origenId.equals(destinoId)) {
                    throw new BusinessException("La bodega origen y destino no pueden ser la misma en una TRANSFERENCIA");
                }
            }
        }
    }

    private void aplicarEfectoEnInventario(TipoMovimiento tipo, Bodega origen, Bodega destino, Producto producto, Integer cantidad) {
        switch (tipo) {
            case ENTRADA -> sumarStock(destino, producto, cantidad);
            case SALIDA -> restarStock(origen, producto, cantidad);
            case TRANSFERENCIA -> {
                restarStock(origen, producto, cantidad);
                sumarStock(destino, producto, cantidad);
            }
        }
    }

    private void sumarStock(Bodega bodega, Producto producto, Integer cantidad) {
        InventarioBodega inventario = obtenerOCrearInventario(bodega, producto);
        inventario.setStock(inventario.getStock() + cantidad);
        inventarioBodegaRepository.save(inventario);
    }

    private void restarStock(Bodega bodega, Producto producto, Integer cantidad) {
        InventarioBodega inventario = inventarioBodegaRepository.findByBodegaIdAndProductoId(bodega.getId(), producto.getId())
                .orElseThrow(() -> new BusinessException(
                        "El producto '" + producto.getNombre() + "' no tiene inventario registrado en la bodega '" + bodega.getNombre() + "'"));
        if (inventario.getStock() < cantidad) {
            throw new BusinessException("Stock insuficiente de '" + producto.getNombre() + "' en la bodega '" + bodega.getNombre()
                    + "' (disponible: " + inventario.getStock() + ", solicitado: " + cantidad + ")");
        }
        inventario.setStock(inventario.getStock() - cantidad);
        inventarioBodegaRepository.save(inventario);
    }

    private InventarioBodega obtenerOCrearInventario(Bodega bodega, Producto producto) {
        Optional<InventarioBodega> existente = inventarioBodegaRepository.findByBodegaIdAndProductoId(bodega.getId(), producto.getId());
        if (existente.isPresent()) {
            return existente.get();
        }
        InventarioBodega nuevo = new InventarioBodega();
        nuevo.setBodega(bodega);
        nuevo.setProducto(producto);
        nuevo.setStock(0);
        return nuevo;
    }
}