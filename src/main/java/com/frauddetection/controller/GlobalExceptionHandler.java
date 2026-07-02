package com.frauddetection.controller;

import java.time.Instant;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    var errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
    log.warn("Validation failed: {}", errors);
    return ResponseEntity.badRequest()
        .body(new ErrorResponse("validation_failed", errors, Instant.now().toString()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(new ErrorResponse("bad_request", ex.getMessage(), Instant.now().toString()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleInternal(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ErrorResponse(
                "internal_error", "An unexpected error occurred", Instant.now().toString()));
  }

  public record ErrorResponse(String error, String message, String timestamp) {}
}
