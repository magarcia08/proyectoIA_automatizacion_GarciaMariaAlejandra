package com.project.springboot.demoproject.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.springboot.demoproject.entities.InventarioBodega;

public interface InventarioBodegaRepository extends JpaRepository<InventarioBodega, Long> {

    List<InventarioBodega> findByBodegaId(Long bodegaId);

    List<InventarioBodega> findByProductoId(Long productoId);

    Optional<InventarioBodega> findByBodegaIdAndProductoId(Long bodegaId, Long productoId);

    // Productos con stock bajo (< umbral) en cualquier bodega
    List<InventarioBodega> findByStockLessThan(Integer stockMinimo);

    List<InventarioBodega> findByBodegaIdAndStockLessThan(Long bodegaId, Integer stockMinimo);

    @Query("SELECT ib.bodega.id, ib.bodega.nombre, COALESCE(SUM(ib.stock), 0) " +
           "FROM InventarioBodega ib GROUP BY ib.bodega.id, ib.bodega.nombre")
    List<Object[]> obtenerStockTotalPorBodega();

    @Query("SELECT COALESCE(SUM(ib.stock), 0) FROM InventarioBodega ib WHERE ib.producto.id = :productoId")
    Integer obtenerStockTotalPorProducto(@Param("productoId") Long productoId);
}
