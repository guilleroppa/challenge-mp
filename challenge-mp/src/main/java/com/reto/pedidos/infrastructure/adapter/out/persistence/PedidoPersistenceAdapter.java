package com.reto.pedidos.infrastructure.adapter.out.persistence;

import com.reto.pedidos.domain.model.Pedido;
import com.reto.pedidos.domain.port.out.PedidoRepositoryPort;
import com.reto.pedidos.infrastructure.adapter.out.persistence.entity.PedidoEntity;
import com.reto.pedidos.infrastructure.adapter.out.persistence.mapper.PedidoEntityMapper;
import com.reto.pedidos.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PedidoPersistenceAdapter implements PedidoRepositoryPort {

    private final PedidoJpaRepository jpaRepository;
    private final PedidoEntityMapper mapper;

    public PedidoPersistenceAdapter(PedidoJpaRepository jpaRepository, PedidoEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void guardarTodos(List<Pedido> pedidos) {
        List<PedidoEntity> entities = pedidos.stream()
                .map(mapper::toEntity)
                .toList();
        jpaRepository.saveAll(entities);
        jpaRepository.flush();
    }

    @Override
    public Set<String> buscarNumerosPedidoExistentes(Set<String> numeros) {
        if (numeros == null || numeros.isEmpty()) return Set.of();
        return jpaRepository.findNumerosPedidoIn(numeros);
    }
}
