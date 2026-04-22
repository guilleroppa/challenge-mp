package com.reto.pedidos.infrastructure.exception;

public class IdempotenciaException extends RuntimeException {
    public IdempotenciaException(String message) {
        super(message);
    }
}
