package com.project.springboot.demoproject.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.springboot.demoproject.entities.MovimientoDetalle;
import com.project.springboot.demoproject.enums.TipoMovimiento;

public interface MovimientoDetalleRepository extends JpaRepository<MovimientoDetalle, Long> {

    List<MovimientoDetalle> findByMovimientoId(Long movimientoId);

    List<MovimientoDetalle> findByProductoId(Long productoId);

    /**
     * LogiTrack IQ: suma de cantidades de un producto, en movimientos de un
     * tipo dado, cuya fecha cae dentro de [inicio, fin]. Se usa para el
     * consumo diario promedio (tipo=SALIDA, ultimos 30 dias calendario en
     * America/Bogota) — ver RiesgoService.
     */
    @Query("SELECT COALESCE(SUM(md.cantidad), 0) FROM MovimientoDetalle md " +
           "WHERE md.producto.id = :productoId AND md.movimiento.tipo = :tipo " +
           "AND md.movimiento.fecha BETWEEN :inicio AND :fin")
    Integer sumarCantidadPorProductoTipoYRango(@Param("productoId") Long productoId,
            @Param("tipo") TipoMovimiento tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

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
