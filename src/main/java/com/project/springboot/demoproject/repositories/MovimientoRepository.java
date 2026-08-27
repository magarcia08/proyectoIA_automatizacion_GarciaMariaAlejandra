package com.project.springboot.demoproject.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.enums.TipoMovimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /**
     * Trae usuario, bodegas y detalles en UNA sola consulta (JOIN FETCH),
     * evitando el problema N+1 al listar movimientos. Usar esta version
     * (o una variante con WHERE) en cualquier endpoint que liste varios
     * movimientos a la vez.
     */
    @Query("""
            SELECT DISTINCT m FROM Movimiento m
            LEFT JOIN FETCH m.usuario
            LEFT JOIN FETCH m.bodegaOrigen
            LEFT JOIN FETCH m.bodegaDestino
            LEFT JOIN FETCH m.detalles d
            LEFT JOIN FETCH d.producto
            ORDER BY m.fecha DESC
            """)
    List<Movimiento> findAllConDetalles();

    List<Movimiento> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Movimiento> findByTipo(TipoMovimiento tipo);

    List<Movimiento> findByUsuarioId(Long usuarioId);

    List<Movimiento> findByBodegaOrigenId(Long bodegaId);

    List<Movimiento> findByBodegaDestinoId(Long bodegaId);

    /** LogiTrack IQ: conteo de movimientos (no de detalles) por tipo en un rango — ver GET /kpis (movimientosAyer). */
    long countByTipoAndFechaBetween(TipoMovimiento tipo, LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT m FROM Movimiento m WHERE m.bodegaOrigen.id = :bodegaId OR m.bodegaDestino.id = :bodegaId")
    List<Movimiento> findByBodegaInvolucrada(@Param("bodegaId") Long bodegaId);


    @Query(value = """
            SELECT DISTINCT m.* FROM movimiento m
            LEFT JOIN movimiento_detalle d ON d.movimiento_id = m.id
            WHERE (CAST(:bodegaId AS bigint) IS NULL
                   OR m.bodega_origen_id = CAST(:bodegaId AS bigint)
                   OR m.bodega_destino_id = CAST(:bodegaId AS bigint))
              AND (CAST(:productoId AS bigint) IS NULL OR d.producto_id = CAST(:productoId AS bigint))
              AND (CAST(:tipoMovimiento AS text) IS NULL OR m.tipo = CAST(:tipoMovimiento AS tipo_movimiento))
              AND (CAST(:fechaInicio AS timestamp) IS NULL OR m.fecha >= CAST(:fechaInicio AS timestamp))
              AND (CAST(:fechaFin AS timestamp) IS NULL OR m.fecha <= CAST(:fechaFin AS timestamp))
            ORDER BY m.fecha DESC
            """, nativeQuery = true)
    List<Movimiento> buscarConFiltros(
            @Param("bodegaId") Long bodegaId,
            @Param("productoId") Long productoId,
            @Param("tipoMovimiento") String tipoMovimiento,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}