package com.project.springboot.demoproject.audit;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UsuarioRepository usuarioRepository;

    /**
     * Devuelve el Usuario autenticado actualmente (segun el token JWT), o vacio
     * si la peticion no tiene un usuario autenticado (por ejemplo, /auth/register).
     */
    public Optional<Usuario> getUsuarioActual() {
        return getUsuarioActualUsername().flatMap(usuarioRepository::findByUsername);
    }

    /**
     * Devuelve solo el username autenticado, leyendo unicamente el SecurityContext
     * (sin tocar la base de datos). Seguro para usarse dentro de callbacks de JPA
     * (@PostPersist/@PostUpdate/@PostRemove), donde una consulta nueva podria
     * forzar un flush anidado de Hibernate y corromper la sesion activa.
     */
    public Optional<String> getUsuarioActualUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.of(auth.getName());
    }
}