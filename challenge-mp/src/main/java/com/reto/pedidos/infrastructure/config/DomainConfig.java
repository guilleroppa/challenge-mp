package com.reto.pedidos.infrastructure.config;

import com.reto.pedidos.domain.service.PedidoValidadorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public PedidoValidadorService pedidoValidadorService() {
        return new PedidoValidadorService();
    }
}
