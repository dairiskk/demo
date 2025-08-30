package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- 400: @Valid on request bodies ----------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage()))
                .collect(toList());

        Map<String, Object> body = base(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Validation failed",
                request.getRequestURI()
        );
        body.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // ---------- 400: @Validated on params/path variables ----------
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<Map<String, String>> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.of(
                        "field", pathOf(v),
                        "message", v.getMessage()))
                .collect(toList());

        Map<String, Object> body = base(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Constraint violation",
                request.getRequestURI()
        );
        body.put("errors", violations);
        return ResponseEntity.badRequest().body(body);
    }

    // ---------- 409: duplicates / DB constraint issues ----------
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> body = base(
                HttpStatus.CONFLICT,
                "Conflict",
                "Duplicate or constraint violation",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ---------- Your explicit errors thrown with ResponseStatusException ----------
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleRSE(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = ex.getStatusCode();
        String errorPhrase = (statusCode instanceof HttpStatus s) ? s.getReasonPhrase() : "Error";

        Map<String, Object> body = base(
                statusCode,
                errorPhrase,
                ex.getReason() == null ? errorPhrase : ex.getReason(),
                request.getRequestURI()
        );
        return ResponseEntity.status(statusCode).body(body);
    }

    // ---------- 500: last-resort fallback ----------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        Map<String, Object> body = base(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Unexpected error",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ---------- helpers ----------
    private static Map<String, Object> base(HttpStatusCode status, String error, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    private static String pathOf(ConstraintViolation<?> v) {
        // e.g. "create.user.email" or "id" → keep the last segment as the "field" name
        String full = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
        int dot = full.lastIndexOf('.');
        return dot >= 0 ? full.substring(dot + 1) : full;
    }
}
