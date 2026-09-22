package com.reto.pedidos.infrastructure.adapter.out.persistence.mapper;

import com.reto.pedidos.domain.model.EstadoPedido;
import com.reto.pedidos.domain.model.Pedido;
import com.reto.pedidos.infrastructure.adapter.out.persistence.entity.PedidoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PedidoEntityMapper {

    @Mapping(target = "estado", expression = "java(pedido.getEstado().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PedidoEntity toEntity(Pedido pedido);

    @Mapping(target = "estado", expression = "java(com.reto.pedidos.domain.model.EstadoPedido.valueOf(entity.getEstado()))")
    Pedido toDomain(PedidoEntity entity);
}
