package com.reto.pedidos.infrastructure.adapter.out.persistence;

import com.reto.pedidos.domain.port.out.IdempotenciaRepositoryPort;
import com.reto.pedidos.infrastructure.adapter.out.persistence.entity.CargaIdempotenciaEntity;
import com.reto.pedidos.infrastructure.adapter.out.persistence.repository.CargaIdempotenciaJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class IdempotenciaPersistenceAdapter implements IdempotenciaRepositoryPort {

    private final CargaIdempotenciaJpaRepository jpaRepository;

    public IdempotenciaPersistenceAdapter(CargaIdempotenciaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existeClave(String idempotencyKey, String archivoHash) {
        return jpaRepository.existsByIdempotencyKeyAndArchivoHash(idempotencyKey, archivoHash);
    }

    @Override
    public void registrarClave(String idempotencyKey, String archivoHash) {
        jpaRepository.save(new CargaIdempotenciaEntity(idempotencyKey, archivoHash));
    }
}
