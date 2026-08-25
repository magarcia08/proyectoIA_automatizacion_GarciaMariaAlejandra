package com.project.springboot.demoproject.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String tipo = "Bearer";
    private String username;
    private String rol;

    public JwtResponse(String token, String username, String rol) {
        this.token = token;
        this.tipo = "Bearer";
        this.username = username;
        this.rol = rol;
    }
}
