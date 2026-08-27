package com.project.springboot.demoproject.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.springboot.demoproject.dto.logitrackiq.ProveedorResponse;
import com.project.springboot.demoproject.repositories.ProveedorRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
@Tag(name = "LogiTrack IQ - Proveedores", description = "Proveedores precargados (data.sql)")
@SecurityRequirement(name = "bearerAuth")
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    @GetMapping
    @Operation(summary = "Listar proveedores")
    public List<ProveedorResponse> listar() {
        return proveedorRepository.findAll().stream().map(ProveedorResponse::desde).toList();
    }
}
