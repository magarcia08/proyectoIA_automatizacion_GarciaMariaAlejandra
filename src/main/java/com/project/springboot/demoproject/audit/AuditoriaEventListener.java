package com.project.springboot.demoproject.audit;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.project.springboot.demoproject.entities.Auditoria;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.repositories.AuditoriaRepository;
import com.project.springboot.demoproject.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Escucha los eventos de auditoria publicados por AuditoriaEntityListener y
 * los persiste en la tabla "auditoria".
 *
 * Se ejecuta DESPUES de que la transaccion principal ya hizo commit, y en una
 * transaccion NUEVA e independiente (REQUIRES_NEW). Todo el acceso a base de
 * datos relacionado con auditoria (resolver el Usuario y guardar el registro)
 * ocurre aqui, nunca dentro de un callback de JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaEventListener {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditoriaEvent(AuditoriaEvent event) {
        try {
            Usuario usuario = resolverUsuario(event.username());
            if (usuario == null) {
                log.warn("No se pudo resolver un usuario responsable para auditar {} id={}",
                        event.entidadAfectada(), event.entidadId());
                return;
            }

            Auditoria auditoria = new Auditoria();
            auditoria.setTipoOperacion(event.tipoOperacion());
            auditoria.setUsuario(usuario);
            auditoria.setEntidadAfectada(event.entidadAfectada());
            auditoria.setEntidadId(event.entidadId());
            auditoria.setValoresAnteriores(event.valoresAnteriores());
            auditoria.setValoresNuevos(event.valoresNuevos());
            auditoriaRepository.save(auditoria);
        } catch (Exception ex) {
            log.error("Error al guardar registro de auditoria para {} id={}: {}",
                    event.entidadAfectada(), event.entidadId(), ex.getMessage(), ex);
        }
    }

    private Usuario resolverUsuario(String username) {
        Optional<Usuario> porUsername = username != null
                ? usuarioRepository.findByUsername(username)
                : Optional.empty();
        return porUsername.or(() -> usuarioRepository.findByUsername("admin")).orElse(null);
    }
}