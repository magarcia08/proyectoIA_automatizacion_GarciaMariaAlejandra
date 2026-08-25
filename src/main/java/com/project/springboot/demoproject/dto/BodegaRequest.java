package com.project.springboot.demoproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BodegaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "La ubicacion es obligatoria")
    @Size(max = 150, message = "La ubicacion no puede superar 150 caracteres")
    private String ubicacion;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 0, message = "La capacidad no puede ser negativa")
    private Integer capacidad;

    @NotBlank(message = "El encargado es obligatorio")
    @Size(max = 100, message = "El encargado no puede superar 100 caracteres")
    private String encargado;
}
