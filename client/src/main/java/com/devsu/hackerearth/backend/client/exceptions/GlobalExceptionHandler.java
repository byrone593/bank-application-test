package com.devsu.hackerearth.backend.client.exceptions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex){
    return build(HttpStatus.NOT_FOUND, ex.getMessage());
}

@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiError> notFound(BusinessException ex){
    return build(HttpStatus.BAD_REQUEST, ex.getMessage());
}

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiError> notFound(MethodArgumentNotValidException ex){
    String msg = ex.getBindingResult().getFieldErrors().stream()
    .map(e -> e.getField() + ": " + e.getDefaultMessage())
    .collect(Collectors.joining(": "));
    return build(HttpStatus.BAD_REQUEST, ex.getMessage());
}

@ExceptionHandler({HttpMessageNotReadableException.class, 
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class})
public ResponseEntity<ApiError> badRequest(Exception ex){
    return build(HttpStatus.BAD_REQUEST, "SOLICITUD ENVÁLIDA: REVISE EL CUERPO O LOS PARÁMETROS");
 }

 @ExceptionHandler(Exception.class)
 public ResponseEntity<ApiError> unexpected(Exception ex){
    log.error("Error no controlado", ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR INTERNO DEL SERVIDOR");
 }

 private ResponseEntity<ApiError> build(HttpStatus status, String message){
    return ResponseEntity.status(status)
    .body(new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message));
 }
}
