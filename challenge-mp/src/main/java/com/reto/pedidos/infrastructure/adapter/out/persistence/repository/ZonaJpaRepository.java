package com.reto.pedidos.infrastructure.adapter.out.persistence.repository;

import com.reto.pedidos.infrastructure.adapter.out.persistence.entity.ZonaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ZonaJpaRepository extends JpaRepository<ZonaEntity, String> {
    List<ZonaEntity> findAllByIdIn(Set<String> ids);
}
