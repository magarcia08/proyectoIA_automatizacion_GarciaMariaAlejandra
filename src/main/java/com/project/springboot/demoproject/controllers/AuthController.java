package com.project.springboot.demoproject.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.audit.CurrentUserProvider;
import com.project.springboot.demoproject.dto.UsuarioResponse;
import com.project.springboot.demoproject.dto.auth.JwtResponse;
import com.project.springboot.demoproject.dto.auth.LoginRequest;
import com.project.springboot.demoproject.dto.auth.RegisterRequest;
import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.security.CustomUserDetailsService;
import com.project.springboot.demoproject.security.JwtService;
import com.project.springboot.demoproject.services.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Login (publico) y registro de usuarios (solo ADMIN/SUPERADMIN)")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar un nuevo usuario. Requiere estar autenticado como ADMIN o SUPERADMIN. "
            + "Un ADMIN solo puede crear EMPLEADO; un SUPERADMIN puede crear ADMIN o EMPLEADO.")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        Usuario creador = currentUserProvider.getUsuarioActual()
                .orElseThrow(() -> new AccessDeniedException("No se pudo identificar al usuario autenticado"));
        UsuarioResponse creado = usuarioService.registrar(request, creador);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion y obtener un token JWT")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String rol = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = jwtService.generarToken(userDetails, rol);

        return ResponseEntity.ok(new JwtResponse(token, request.getUsername(), rol));
    }
}
