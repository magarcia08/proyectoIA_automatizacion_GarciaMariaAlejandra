package com.project.springboot.demoproject.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.springboot.demoproject.audit.CurrentUserProvider;
import com.project.springboot.demoproject.dto.logitrackiq.AccionSugeridaDto;
import com.project.springboot.demoproject.dto.logitrackiq.AlertaDto;
import com.project.springboot.demoproject.dto.logitrackiq.ResumenPanelRequest;
import com.project.springboot.demoproject.dto.logitrackiq.ResumenPanelResponse;
import com.project.springboot.demoproject.entities.ResumenPanel;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;
import com.project.springboot.demoproject.repositories.ResumenPanelRepository;

import lombok.RequiredArgsConstructor;

/**
 * Valida y publica el resumen del panel (LogiTrack IQ). Un JSON invalido
 * responde 400 y el ultimo resumen VALIDO permanece disponible (nunca se
 * persiste el intento fallido: toda la validacion ocurre ANTES de tocar
 * el repositorio). Ver docs/sdd/02-especificacion.md.
 */
@Service
@RequiredArgsConstructor
public class PanelResumenService {

    private final ResumenPanelRepository resumenPanelRepository;
    private final ProductoRepository productoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final BodegaRepository bodegaRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResumenPanelResponse publicar(ResumenPanelRequest request) {
        LocalDate hoy = LocalDate.now(RiesgoService.ZONA_BOGOTA);
        if (!hoy.equals(request.getFecha())) {
            throw new BusinessException("La fecha del resumen debe ser la fecha actual en America/Bogota (" + hoy + ")");
        }
        request.getAlertas().forEach(this::validarAlerta);
        request.getAccionesSugeridas().forEach(this::validarAccion);

        Usuario autor = currentUserProvider.getUsuarioActual()
                .orElseThrow(() -> new BusinessException("No hay un usuario autenticado para publicar el resumen"));

        // Un unico resumen valido por fecha: si ya existe, se REEMPLAZA (misma fila,
        // el listener de auditoria registra el UPDATE); si no, se crea.
        ResumenPanel resumen = resumenPanelRepository.findByFecha(hoy).orElseGet(ResumenPanel::new);
        resumen.setFecha(hoy);
        resumen.setContenidoJson(serializar(request));
        resumen.setAutor(autor);
        resumen.setCreadoEn(LocalDateTime.now());

        return aRespuesta(resumenPanelRepository.save(resumen));
    }

    @Transactional(readOnly = true)
    public ResumenPanelResponse obtenerUltimo() {
        ResumenPanel resumen = resumenPanelRepository.findFirstByOrderByFechaDesc()
                .orElseThrow(() -> new ResourceNotFoundException("Todavia no se ha publicado ningun resumen del panel"));
        return aRespuesta(resumen);
    }

    // ------------------------------------------------------------------

    private void validarAlerta(AlertaDto alerta) {
        int enlaces = contarNoNulos(alerta.getProductoId(), alerta.getOrdenId(), alerta.getBodegaId());
        if (enlaces < 1) {
            throw new BusinessException("Cada alerta debe enlazar al menos un identificador (productoId, ordenId o bodegaId)");
        }
        validarExistencia(alerta.getProductoId(), alerta.getOrdenId(), alerta.getBodegaId());
    }

    private void validarAccion(AccionSugeridaDto accion) {
        int enlaces = contarNoNulos(accion.getProductoId(), accion.getOrdenId(), accion.getBodegaId());
        if (enlaces != 1) {
            throw new BusinessException("Cada accion sugerida debe enlazar EXACTAMENTE un identificador (ordenId, productoId o bodegaId)");
        }
        validarExistencia(accion.getProductoId(), accion.getOrdenId(), accion.getBodegaId());
    }

    private int contarNoNulos(Object... valores) {
        return (int) Arrays.stream(valores).filter(Objects::nonNull).count();
    }

    private void validarExistencia(Long productoId, Long ordenId, Long bodegaId) {
        if (productoId != null && !productoRepository.existsById(productoId)) {
            throw new BusinessException("El producto con id " + productoId + " no existe");
        }
        if (ordenId != null && !ordenCompraRepository.existsById(ordenId)) {
            throw new BusinessException("La orden con id " + ordenId + " no existe");
        }
        if (bodegaId != null && !bodegaRepository.existsById(bodegaId)) {
            throw new BusinessException("La bodega con id " + bodegaId + " no existe");
        }
    }

    private String serializar(ResumenPanelRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new BusinessException("No se pudo serializar el resumen: " + e.getMessage());
        }
    }

    private ResumenPanelResponse aRespuesta(ResumenPanel resumen) {
        try {
            ResumenPanelRequest contenido = objectMapper.readValue(resumen.getContenidoJson(), ResumenPanelRequest.class);
            return new ResumenPanelResponse(contenido.getFecha(), contenido.getNarrativa(), contenido.getAlertas(),
                    contenido.getAccionesSugeridas(), resumen.getAutor().getUsername(), resumen.getCreadoEn());
        } catch (Exception e) {
            throw new BusinessException("No se pudo leer el resumen guardado: " + e.getMessage());
        }
    }
}
