package com.project.springboot.demoproject.audit;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

/**
 * Genera una representacion JSON "plana" y segura de una entidad para guardarla
 * en auditoria.valores_anteriores / valores_nuevos.
 *
 * No serializa colecciones (@OneToMany/@ManyToMany) para evitar disparar
 * carga perezosa innecesaria y ciclos infinitos. Las relaciones @ManyToOne
 * se reducen a "<campo>Id": <id> en vez de serializar el objeto completo.
 * Tampoco incluye la contraseña de Usuario.
 */
public final class AuditSnapshotUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AuditSnapshotUtil() {
    }

    public static String toJson(Object entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Object> map = new TreeMap<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(ManyToMany.class)) {
                continue; // colecciones: se omiten
            }
            if ("password".equalsIgnoreCase(field.getName())) {
                map.put(field.getName(), "***");
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value == null) {
                    map.put(field.getName(), null);
                    continue;
                }
                if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                    map.put(field.getName() + "Id", extractId(value));
                    continue;
                }
                map.put(field.getName(), value);
            } catch (Exception e) {
                map.put(field.getName(), "<no disponible>");
            }
        }
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            return map.toString();
        }
    }

    private static Object extractId(Object relatedEntity) {
        try {
            Field idField = relatedEntity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return idField.get(relatedEntity);
        } catch (Exception e) {
            // Proxy no inicializado sin acceso directo al campo id: se ignora
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static Map<String, Object> ordered(Map<String, Object> map) {
        return new LinkedHashMap<>(map);
    }
}
