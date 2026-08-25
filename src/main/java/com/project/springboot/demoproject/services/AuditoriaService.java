package com.project.springboot.demoproject.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.AuditoriaResponse;
import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.AuditoriaRepository;

import lombok.RequiredArgsConstructor;

/** Servicio de solo lectura: los registros de auditoria los crea AuditoriaEntityListener. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public List<AuditoriaResponse> listarTodas() {
        return auditoriaRepository.findAll().stream().map(AuditoriaResponse::desde).toList();
    }

    public AuditoriaResponse obtenerPorId(Long id) {
        return auditoriaRepository.findById(id)
                .map(AuditoriaResponse::desde)
                .orElseThrow(() -> ResourceNotFoundException.of("Auditoria", id));
    }

    public List<AuditoriaResponse> buscarPorUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId).stream()
                .map(AuditoriaResponse::desde).toList();
    }

    public List<AuditoriaResponse> buscarPorTipoOperacion(TipoOperacionAuditoria tipo) {
        return auditoriaRepository.findByTipoOperacion(tipo).stream().map(AuditoriaResponse::desde).toList();
    }

    public List<AuditoriaResponse> buscarPorEntidad(String entidadAfectada) {
        return auditoriaRepository.findByEntidadAfectada(entidadAfectada).stream()
                .map(AuditoriaResponse::desde).toList();
    }

    public List<AuditoriaResponse> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return auditoriaRepository.findByFechaHoraBetween(inicio, fin).stream()
                .map(AuditoriaResponse::desde).toList();
    }
}
