package com.sistema.cadastro.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    private final HttpStatus status;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s nÃ£o encontrado com %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.status = HttpStatus.NOT_FOUND;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = "Recurso";
        this.fieldName = "id";
        this.fieldValue = null;
        this.status = HttpStatus.NOT_FOUND;
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceName = "Recurso";
        this.fieldName = "id";
        this.fieldValue = null;
        this.status = HttpStatus.NOT_FOUND;
    }

    public String getResourceName() { return this.resourceName; }
    public String getFieldName() { return this.fieldName; }
    public Object getFieldValue() { return this.fieldValue; }
    public HttpStatus getStatus() { return this.status; }
}
