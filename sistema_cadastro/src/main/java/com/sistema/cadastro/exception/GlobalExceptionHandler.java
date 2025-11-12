package com.sistema.cadastro.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Erro de validaÃ§Ã£o",
                "VALIDATION_ERROR",
                request.getRequestURI(),
                errors
        );

        log.error("Erro de validaÃ§Ã£o: {}", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Erro de validaÃ§Ã£o de constraint",
                "CONSTRAINT_VIOLATION",
                request.getRequestURI(),
                errors
        );

        log.error("Erro de constraint: {}", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        Map<String, Object> response = createErrorResponse(
                ex.getStatus(),
                ex.getMessage(),
                ex.getErrorCode(),
                request.getRequestURI(),
                null
        );

        log.error("Erro de negÃ³cio: {}", ex.getMessage());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        
        Map<String, Object> details = new HashMap<>();
        details.put("resource", ex.getResourceName());
        details.put("field", ex.getFieldName());
        details.put("value", ex.getFieldValue());

        Map<String, Object> response = createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                "RESOURCE_NOT_FOUND",
                request.getRequestURI(),
                details
        );

        log.error("Recurso nÃ£o encontrado: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        String message = "Erro de integridade de dados";
        String details = ex.getMostSpecificCause().getMessage();
        
        if (details != null) {
            if (details.contains("unique constraint") || details.contains("duplicate key")) {
                message = "Registro duplicado. Este item jÃ¡ existe no sistema";
            } else if (details.contains("foreign key constraint")) {
                message = "NÃ£o Ã© possÃ­vel excluir este registro pois ele estÃ¡ sendo referenciado";
            } else if (details.contains("check constraint")) {
                message = "Erro de validaÃ§Ã£o de dados";
            }
        }

        Map<String, Object> response = createErrorResponse(
                HttpStatus.CONFLICT,
                message,
                "DATA_INTEGRITY_VIOLATION",
                request.getRequestURI(),
                details
        );

        log.error("Erro de integridade de dados: {}", details);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String message = String.format("Valor invÃ¡lido para o parÃ¢metro '%s': '%s'",
                ex.getName(), ex.getValue());

        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                "TYPE_MISMATCH",
                request.getRequestURI(),
                null
        );

        log.error("Erro de tipo: {}", message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                "ILLEGAL_ARGUMENT",
                request.getRequestURI(),
                null
        );

        log.error("Argumento invÃ¡lido: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        
        Map<String, Object> response = createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                "ILLEGAL_STATE",
                request.getRequestURI(),
                null
        );

        log.error("Estado invÃ¡lido: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request, HttpServletRequest httpRequest) {
        
        Map<String, Object> response = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno do servidor",
                "INTERNAL_SERVER_ERROR",
                httpRequest.getRequestURI(),
                null
        );

        log.error("Erro nÃ£o tratado: ", ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String message, 
                                                     String errorCode, String path, Object details) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        response.put("errorCode", errorCode);
        response.put("path", path);
        
        if (details != null) {
            response.put("details", details);
        }
        
        return response;
    }
}
