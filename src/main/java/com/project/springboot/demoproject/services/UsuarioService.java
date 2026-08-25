package com.project.springboot.demoproject.services;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.springboot.demoproject.dto.UsuarioResponse;
import com.project.springboot.demoproject.dto.auth.RegisterRequest;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.Rol;
import com.project.springboot.demoproject.exception.BusinessException;
import com.project.springboot.demoproject.exception.DuplicateResourceException;
import com.project.springboot.demoproject.exception.ResourceNotFoundException;
import com.project.springboot.demoproject.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Crea un nuevo usuario. Quien lo crea (creador) determina que roles puede asignar:
     *  - SUPERADMIN: puede crear ADMIN o EMPLEADO (no otro SUPERADMIN por este endpoint).
     *  - ADMIN: solo puede crear EMPLEADO.
     *  - Cualquier otro caso: rechazado (en la practica, el endpoint ya esta
     *    protegido con @PreAuthorize para que solo ADMIN/SUPERADMIN lleguen aqui).
     */
    @Transactional
    public UsuarioResponse registrar(RegisterRequest request, Usuario creador) {
        validarPermisoDeCreacion(creador, request.getRol());

        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Ya existe un usuario con el username '" + request.getUsername() + "'");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email '" + request.getEmail() + "'");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());
        usuario.setActivo(true);

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    private void validarPermisoDeCreacion(Usuario creador, Rol rolSolicitado) {
        if (creador == null) {
            throw new BusinessException("No se pudo identificar al usuario que realiza la creacion");
        }

        if (creador.getRol() == Rol.ADMIN && rolSolicitado != Rol.EMPLEADO) {
            throw new BusinessException("Un ADMIN solo puede crear usuarios con rol EMPLEADO");
        }

        if (creador.getRol() == Rol.SUPERADMIN && rolSolicitado == Rol.SUPERADMIN) {
            throw new BusinessException("No se pueden crear nuevos SUPERADMIN desde este endpoint");
        }

        if (creador.getRol() == Rol.EMPLEADO) {
            throw new BusinessException("Un EMPLEADO no tiene permisos para crear usuarios");
        }
    }

    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    public UsuarioResponse obtenerPorId(Long id) {
        return UsuarioResponse.desde(buscarPorId(id));
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Usuario", id));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(activo);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }
}
