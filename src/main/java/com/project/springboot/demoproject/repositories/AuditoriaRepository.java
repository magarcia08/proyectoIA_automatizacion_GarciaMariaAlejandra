package com.project.springboot.demoproject.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.springboot.demoproject.entities.Auditoria;
import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    // Metodo derivado simple: Spring Data lo traduce solo a
    // "SELECT a FROM Auditoria a WHERE a.usuario.id = :usuarioId"
    List<Auditoria> findByUsuarioId(Long usuarioId);

    List<Auditoria> findByTipoOperacion(TipoOperacionAuditoria tipoOperacion);

    List<Auditoria> findByUsuarioIdAndTipoOperacion(Long usuarioId, TipoOperacionAuditoria tipoOperacion);

    List<Auditoria> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Auditoria> findByEntidadAfectada(String entidadAfectada);

    List<Auditoria> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

   
    @Query(value = """
            SELECT * FROM auditoria a
            WHERE (CAST(:productoId AS bigint) IS NULL
                   OR (a.entidad_afectada = 'producto' AND a.entidad_id = CAST(:productoId AS bigint)))
              AND (CAST(:fechaInicio AS timestamp) IS NULL OR a.fecha_hora >= CAST(:fechaInicio AS timestamp))
              AND (CAST(:fechaFin AS timestamp) IS NULL OR a.fecha_hora <= CAST(:fechaFin AS timestamp))
              AND (CAST(:campoModificado AS text) IS NULL
                   OR LOWER(a.valores_anteriores) LIKE '%"' || LOWER(CAST(:campoModificado AS text)) || '":%'
                   OR LOWER(a.valores_nuevos) LIKE '%"' || LOWER(CAST(:campoModificado AS text)) || '":%')
            ORDER BY a.fecha_hora DESC
            """, nativeQuery = true)
    List<Auditoria> buscarConFiltros(
            @Param("productoId") Long productoId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            @Param("campoModificado") String campoModificado);
}