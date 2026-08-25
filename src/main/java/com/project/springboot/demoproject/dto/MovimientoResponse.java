package com.project.springboot.demoproject.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.project.springboot.demoproject.entities.Movimiento;
import com.project.springboot.demoproject.enums.TipoMovimiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponse {
    private Long id;
    private LocalDateTime fecha;
    private TipoMovimiento tipo;
    private Long usuarioId;
    private String usuarioUsername;
    private Long bodegaOrigenId;
    private String bodegaOrigenNombre;
    private Long bodegaDestinoId;
    private String bodegaDestinoNombre;
    private List<MovimientoDetalleResponse> detalles;

    public static MovimientoResponse desde(Movimiento m) {
        MovimientoResponse r = new MovimientoResponse();
        r.setId(m.getId());
        r.setFecha(m.getFecha());
        r.setTipo(m.getTipo());
        r.setUsuarioId(m.getUsuario().getId());
        r.setUsuarioUsername(m.getUsuario().getUsername());
        if (m.getBodegaOrigen() != null) {
            r.setBodegaOrigenId(m.getBodegaOrigen().getId());
            r.setBodegaOrigenNombre(m.getBodegaOrigen().getNombre());
        }
        if (m.getBodegaDestino() != null) {
            r.setBodegaDestinoId(m.getBodegaDestino().getId());
            r.setBodegaDestinoNombre(m.getBodegaDestino().getNombre());
        }
        r.setDetalles(m.getDetalles().stream().map(MovimientoDetalleResponse::desde).toList());
        return r;
    }
}
