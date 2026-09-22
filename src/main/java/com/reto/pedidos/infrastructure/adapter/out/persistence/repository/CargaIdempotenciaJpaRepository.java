package com.reto.pedidos.infrastructure.adapter.out.persistence.repository;

import com.reto.pedidos.infrastructure.adapter.out.persistence.entity.CargaIdempotenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CargaIdempotenciaJpaRepository extends JpaRepository<CargaIdempotenciaEntity, UUID> {
    boolean existsByIdempotencyKeyAndArchivoHash(String idempotencyKey, String archivoHash);
}
