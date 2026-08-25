package com.project.springboot.demoproject.audit;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.Transient;

import com.project.springboot.demoproject.enums.TipoOperacionAuditoria;

public class AuditoriaEntityListener {

    @Transient
    private String snapshotAnterior;

    @PostLoad
    public void onPostLoad(Object entity) {
        this.snapshotAnterior = AuditSnapshotUtil.toJson(entity);
    }

    @PostPersist
    public void onPostPersist(Object entity) {
        registrar(entity, TipoOperacionAuditoria.INSERT, null, AuditSnapshotUtil.toJson(entity));
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        registrar(entity, TipoOperacionAuditoria.UPDATE, snapshotAnterior, AuditSnapshotUtil.toJson(entity));
    }

    @PostRemove
    public void onPostRemove(Object entity) {
        registrar(entity, TipoOperacionAuditoria.DELETE, AuditSnapshotUtil.toJson(entity), null);
    }

    private void registrar(Object entity, TipoOperacionAuditoria tipo, String anteriores, String nuevos) {
        if (!(entity instanceof Auditable auditable)) {
            return;
        }

        ApplicationEventPublisherHolder publisherHolder = SpringContext.getBean(ApplicationEventPublisherHolder.class);
        CurrentUserProvider currentUserProvider = SpringContext.getBean(CurrentUserProvider.class);
        if (publisherHolder == null || currentUserProvider == null) {
            return;
        }

        // IMPORTANTE: solo se lee el SecurityContext (memoria), NUNCA se consulta
        // la base de datos aqui. Cualquier query dentro de este callback puede
        // forzar un flush anidado de Hibernate y corromper la sesion activa.
        String username = currentUserProvider.getUsuarioActualUsername().orElse(null);

        publisherHolder.publish(new AuditoriaEvent(
                username,
                auditable.getNombreEntidad(),
                auditable.getEntidadId(),
                tipo,
                anteriores,
                nuevos
        ));
    }
}