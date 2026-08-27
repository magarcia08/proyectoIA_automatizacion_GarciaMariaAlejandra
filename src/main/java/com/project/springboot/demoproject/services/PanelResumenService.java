package com.project.springboot.demoproject.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.audit.CurrentUserProvider;
import com.project.springboot.demoproject.dto.logitrackiq.ResumenPanelRequest;
import com.project.springboot.demoproject.dto.logitrackiq.ResumenPanelResponse;
import com.project.springboot.demoproject.repositories.BodegaRepository;
import com.project.springboot.demoproject.repositories.OrdenCompraRepository;
import com.project.springboot.demoproject.repositories.ProductoRepository;
import com.project.springboot.demoproject.repositories.ResumenPanelRepository;
import com.project.springboot.demoproject.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Valida y publica el resumen del panel (LogiTrack IQ). Un JSON invalido
 * responde 400 y el ultimo resumen VALIDO permanece disponible (nunca se
 * persiste el intento fallido). Ver docs/sdd/02-especificacion.md.
 *
 * FASE 2: solo la firma de los metodos, sin logica (ver RiesgoService).
 */
@Service
@RequiredArgsConstructor
public class PanelResumenService {

    private final ResumenPanelRepository resumenPanelRepository;
    private final ProductoRepository productoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final BodegaRepository bodegaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public ResumenPanelResponse publicar(ResumenPanelRequest request) {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }

    @Transactional(readOnly = true)
    public ResumenPanelResponse obtenerUltimo() {
        throw new UnsupportedOperationException("Pendiente de implementar en la fase 'feat: implement LogiTrack IQ rules'");
    }
}
