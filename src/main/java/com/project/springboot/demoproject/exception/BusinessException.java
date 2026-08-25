package com.project.springboot.demoproject.exception;

/** Se lanza cuando una regla de negocio no se cumple (produce HTTP 400). Ej: stock insuficiente. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
