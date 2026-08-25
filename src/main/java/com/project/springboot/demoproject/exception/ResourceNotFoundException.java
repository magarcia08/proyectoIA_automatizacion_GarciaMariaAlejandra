package com.project.springboot.demoproject.exception;

/** Se lanza cuando no se encuentra una entidad por su id (produce HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entidad, Object id) {
        return new ResourceNotFoundException(entidad + " con id " + id + " no fue encontrado(a)");
    }
}
