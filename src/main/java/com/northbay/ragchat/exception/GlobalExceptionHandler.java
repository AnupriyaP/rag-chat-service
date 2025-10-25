package com.northbay.ragchat.exception;

import com.northbay.ragchat.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j; // ✅ NEW IMPORT
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j // ✅ ADDED Lombok logger
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle JpaRepository Entity Not Found Errors (404)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        log.warn("Not Found Error: {} - {}", req.getRequestURI(), ex.getMessage()); // ✅ LOGGED
        ErrorResponse error = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ErrorResponse.ErrorCodeEnum.SESSION_NOT_FOUND,
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 2. Handle Bean Validation Errors (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation Error: {} - {}", req.getRequestURI(), validationErrors); // ✅ LOGGED

        ErrorResponse error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed: " + validationErrors,
                ErrorResponse.ErrorCodeEnum.INVALID_INPUT,
                req.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 3. Catch-all for Generic Runtime Exceptions (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        // Log the full stack trace for unexpected errors
        log.error("Unhandled Internal Server Error for path: {}", req.getRequestURI(), ex); // ✅ LOGGED

        ErrorResponse error = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.",
                ErrorResponse.ErrorCodeEnum.INTERNAL_ERROR,
                req.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String message, ErrorResponse.ErrorCodeEnum errorCode, String path) {
        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
        error.setMessage(message);
        error.setPath(path);
        try {
            error.setErrorCode(errorCode);
        } catch (Exception e) { /* ignore if ErrorCodeEnum doesn't exist */ }
        return error;
    }
}