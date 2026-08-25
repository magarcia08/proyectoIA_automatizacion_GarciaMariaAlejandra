package com.project.springboot.demoproject.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.springboot.demoproject.entities.MovimientoDetalle;

public interface MovimientoDetalleRepository extends JpaRepository<MovimientoDetalle, Long> {

    List<MovimientoDetalle> findByMovimientoId(Long movimientoId);

    List<MovimientoDetalle> findByProductoId(Long productoId);

    @Query("SELECT md.producto.id, md.producto.nombre, SUM(md.cantidad) as total " +
           "FROM MovimientoDetalle md " +
           "GROUP BY md.producto.id, md.producto.nombre " +
           "ORDER BY total DESC")
    List<Object[]> obtenerProductosMasMovidos();

    @Query("SELECT md.producto.id, md.producto.nombre, SUM(md.cantidad) as total " +
           "FROM MovimientoDetalle md " +
           "WHERE md.movimiento.fecha BETWEEN :inicio AND :fin " +
           "GROUP BY md.producto.id, md.producto.nombre " +
           "ORDER BY total DESC")
    List<Object[]> obtenerProductosMasMovidosPorFecha(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
