package com.reto.pedidos.domain.port.out;

public interface IdempotenciaRepositoryPort {
    boolean existeClave(String idempotencyKey, String archivoHash);
    void registrarClave(String idempotencyKey, String archivoHash);
}
