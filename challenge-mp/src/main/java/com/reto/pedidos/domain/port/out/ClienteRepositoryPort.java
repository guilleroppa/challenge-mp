package com.reto.pedidos.domain.port.out;

import com.reto.pedidos.domain.model.Cliente;
import java.util.Map;
import java.util.Set;

public interface ClienteRepositoryPort {
    Map<String, Cliente> buscarPorIds(Set<String> ids);
}
