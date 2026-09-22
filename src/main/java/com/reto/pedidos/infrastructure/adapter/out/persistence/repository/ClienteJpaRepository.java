package com.reto.pedidos.infrastructure.adapter.out.persistence.repository;

import com.reto.pedidos.infrastructure.adapter.out.persistence.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, String> {
    List<ClienteEntity> findAllByIdIn(Set<String> ids);
}
