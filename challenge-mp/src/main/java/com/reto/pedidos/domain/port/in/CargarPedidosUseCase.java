package com.reto.pedidos.domain.port.in;

import com.reto.pedidos.domain.model.ResultadoCarga;
import java.io.InputStream;

public interface CargarPedidosUseCase {
    ResultadoCarga cargar(InputStream csv, String idempotencyKey);
}
