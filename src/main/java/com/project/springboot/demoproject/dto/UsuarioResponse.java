package com.project.springboot.demoproject.dto;

import java.time.LocalDateTime;

import com.project.springboot.demoproject.entities.Usuario;
import com.project.springboot.demoproject.enums.Rol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Version publica de Usuario (nunca se expone el password). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String username;
    private String email;
    private Rol rol;
    private Boolean activo;
    private LocalDateTime creadoEn;

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRol(), u.getActivo(), u.getCreadoEn());
    }
}
