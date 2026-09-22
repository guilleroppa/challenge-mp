package com.reto.pedidos.infrastructure.adapter.out.persistence.repository;

import com.reto.pedidos.infrastructure.adapter.out.persistence.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, UUID> {

    @Query("SELECT p.numeroPedido FROM PedidoEntity p WHERE p.numeroPedido IN :numeros")
    Set<String> findNumerosPedidoIn(@Param("numeros") Set<String> numeros);
}
