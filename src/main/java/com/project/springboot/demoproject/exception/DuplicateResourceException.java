package com.project.springboot.demoproject.exception;

/** Se lanza cuando se intenta crear un recurso que ya existe (username/email duplicado, etc). Produce HTTP 409. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
