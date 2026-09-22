package com.reto.pedidos.infrastructure.config;

import com.reto.pedidos.infrastructure.adapter.in.rest.dto.ApiErrorResponse;
import com.reto.pedidos.infrastructure.exception.IdempotenciaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IdempotenciaException.class)
    public ResponseEntity<ApiErrorResponse> handleIdempotencia(IdempotenciaException ex) {
        log.warn("Solicitud idempotente rechazada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        "IDEMPOTENCIA_DUPLICADA",
                        ex.getMessage(),
                        null,
                        MDC.get("correlationId")
                ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        "HEADER_REQUERIDO",
                        "Header requerido faltante: " + ex.getHeaderName(),
                        null,
                        MDC.get("correlationId")
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiErrorResponse(
                        "ARCHIVO_MUY_GRANDE",
                        "El archivo supera el tamaño máximo permitido",
                        null,
                        MDC.get("correlationId")
                ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        "ERROR_INTERNO",
                        "Error interno del servidor",
                        List.of(ex.getMessage()),
                        MDC.get("correlationId")
                ));
    }
}
