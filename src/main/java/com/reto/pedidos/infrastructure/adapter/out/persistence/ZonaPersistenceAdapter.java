package com.reto.pedidos.infrastructure.adapter.out.persistence;

import com.reto.pedidos.domain.model.Zona;
import com.reto.pedidos.domain.port.out.ZonaRepositoryPort;
import com.reto.pedidos.infrastructure.adapter.out.persistence.repository.ZonaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ZonaPersistenceAdapter implements ZonaRepositoryPort {

    private final ZonaJpaRepository jpaRepository;

    public ZonaPersistenceAdapter(ZonaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Map<String, Zona> buscarPorIds(Set<String> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return jpaRepository.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(
                        e -> e.getId(),
                        e -> new Zona(e.getId(), e.isSoporteRefrigeracion())
                ));
    }
}
