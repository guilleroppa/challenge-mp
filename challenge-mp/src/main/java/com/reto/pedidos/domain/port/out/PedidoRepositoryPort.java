package com.reto.pedidos.domain.port.out;

import com.reto.pedidos.domain.model.Pedido;
import java.util.List;
import java.util.Set;

public interface PedidoRepositoryPort {
    void guardarTodos(List<Pedido> pedidos);
    Set<String> buscarNumerosPedidoExistentes(Set<String> numeros);
}
