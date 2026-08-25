package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.InventarioBodegaRequest;
import com.project.springboot.demoproject.dto.InventarioBodegaResponse;
import com.project.springboot.demoproject.dto.reportes.StockBajoDto;
import com.project.springboot.demoproject.entities.Bodega;
import com.project.springboot.demoproject.entities.InventarioBodega;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.exception.DuplicateResourceException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioBodegaService {

    private static final int UMBRAL_STOCK_BAJO = 10;

    private final InventarioBodegaRepository inventarioBodegaRepository;
    private final BodegaService bodegaService;
    private final ProductoService productoService;

    public List<InventarioBodegaResponse> listarTodo() {
        return inventarioBodegaRepository.findAll().stream().map(InventarioBodegaResponse::desde).toList();
    }

    public List<InventarioBodegaResponse> listarPorBodega(Long bodegaId) {
        return inventarioBodegaRepository.findByBodegaId(bodegaId).stream()
                .map(InventarioBodegaResponse::desde).toList();
    }

    public InventarioBodegaResponse obtenerPorId(Long id) {
        return InventarioBodegaResponse.desde(buscarPorId(id));
    }

    public InventarioBodega buscarPorId(Long id) {
        return inventarioBodegaRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Registro de inventario", id));
    }

    @Transactional
    public InventarioBodegaResponse crear(InventarioBodegaRequest request) {
        Bodega bodega = bodegaService.buscarPorId(request.getBodegaId());
        Producto producto = productoService.buscarPorId(request.getProductoId());

        inventarioBodegaRepository.findByBodegaIdAndProductoId(bodega.getId(), producto.getId())
                .ifPresent(existing -> {
                    throw new DuplicateResourceException(
                            "Ya existe un registro de inventario para ese producto en esa bodega (id " + existing.getId() + ")");
                });

        InventarioBodega inventario = new InventarioBodega();
        inventario.setBodega(bodega);
        inventario.setProducto(producto);
        inventario.setStock(request.getStock());
        return InventarioBodegaResponse.desde(inventarioBodegaRepository.save(inventario));
    }

    @Transactional
    public InventarioBodegaResponse actualizarStock(Long id, Integer nuevoStock) {
        InventarioBodega inventario = buscarPorId(id);
        inventario.setStock(nuevoStock);
        return InventarioBodegaResponse.desde(inventarioBodegaRepository.save(inventario));
    }

    @Transactional
    public void eliminar(Long id) {
        inventarioBodegaRepository.delete(buscarPorId(id));
    }

    /** Reporte: productos con stock bajo (< 10 unidades) en cualquier bodega. */
    public List<StockBajoDto> productosConStockBajo() {
        return inventarioBodegaRepository.findByStockLessThan(UMBRAL_STOCK_BAJO).stream()
                .map(ib -> new StockBajoDto(
                        ib.getBodega().getId(), ib.getBodega().getNombre(),
                        ib.getProducto().getId(), ib.getProducto().getNombre(),
                        ib.getStock()))
                .toList();
    }
}
