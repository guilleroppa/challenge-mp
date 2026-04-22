package com.reto.pedidos.infrastructure.adapter.out.persistence;

import com.reto.pedidos.domain.model.Cliente;
import com.reto.pedidos.domain.port.out.ClienteRepositoryPort;
import com.reto.pedidos.infrastructure.adapter.out.persistence.repository.ClienteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ClientePersistenceAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository jpaRepository;

    public ClientePersistenceAdapter(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Map<String, Cliente> buscarPorIds(Set<String> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return jpaRepository.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(
                        e -> e.getId(),
                        e -> new Cliente(e.getId(), e.isActivo())
                ));
    }
}
