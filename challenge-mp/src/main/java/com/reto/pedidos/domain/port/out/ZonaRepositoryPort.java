package com.reto.pedidos.domain.port.out;

import com.reto.pedidos.domain.model.Zona;
import java.util.Map;
import java.util.Set;

public interface ZonaRepositoryPort {
    Map<String, Zona> buscarPorIds(Set<String> ids);
}
