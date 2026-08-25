package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.ProductoRequest;
import com.project.springboot.demoproject.dto.ProductoResponse;
import com.project.springboot.demoproject.entities.Producto;
import com.project.springboot.demoproject.exception.DuplicateResourceException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.InventarioBodegaRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final InventarioBodegaRepository inventarioBodegaRepository;

    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream()
                .map(p -> ProductoResponse.desde(p, stockTotal(p.getId())))
                .toList();
    }

    public ProductoResponse obtenerPorId(Long id) {
        Producto producto = buscarPorId(id);
        return ProductoResponse.desde(producto, stockTotal(id));
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Producto", id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (productoRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe un producto con el nombre '" + request.getNombre() + "'");
        }
        Producto producto = new Producto();
        mapear(request, producto);
        Producto guardado = productoRepository.save(producto);
        return ProductoResponse.desde(guardado, 0);
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarPorId(id);
        mapear(request, producto);
        Producto guardado = productoRepository.save(producto);
        return ProductoResponse.desde(guardado, stockTotal(id));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        productoRepository.delete(producto);
    }

    public List<ProductoResponse> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria).stream()
                .map(p -> ProductoResponse.desde(p, stockTotal(p.getId())))
                .toList();
    }

    public List<ProductoResponse> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(p -> ProductoResponse.desde(p, stockTotal(p.getId())))
                .toList();
    }

    private Integer stockTotal(Long productoId) {
        Integer total = inventarioBodegaRepository.obtenerStockTotalPorProducto(productoId);
        return total == null ? 0 : total;
    }

    private void mapear(ProductoRequest request, Producto producto) {
        producto.setNombre(request.getNombre());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
    }
}
