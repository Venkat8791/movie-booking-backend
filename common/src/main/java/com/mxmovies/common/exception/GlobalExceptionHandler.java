package com.mxmovies.common.exception;

import com.mxmovies.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request){
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 400 Bad Request
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request){
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // 400 Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request){
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 401 Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request){
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    // 403 forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request){
        return build(HttpStatus.FORBIDDEN, "Access Denied", request);
    }


    // ── 401 Bad credentials ───────────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

//    // ── 409 DB unique constraint violation ────────────────────────────────────
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<ErrorResponse> handleDataIntegrity(
//            DataIntegrityViolationException ex,
//            HttpServletRequest request) {
//        return build(HttpStatus.CONFLICT,
//                "Data integrity violation — duplicate or invalid data", request);
//    }

    // ── 500 Catch-all ─────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later.", request);
    }

    //Builder
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request){
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .status(status.value())
                        .error(status.name())
                        .message(message)
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
