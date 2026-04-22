package com.reto.pedidos.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Pedido {

    private UUID id;
    private String numeroPedido;
    private String clienteId;
    private String zonaId;
    private LocalDate fechaEntrega;
    private EstadoPedido estado;
    private boolean requiereRefrigeracion;

    public Pedido() {}

    public Pedido(String numeroPedido, String clienteId, String zonaId,
                  LocalDate fechaEntrega, EstadoPedido estado, boolean requiereRefrigeracion) {
        this.id = UUID.randomUUID();
        this.numeroPedido = numeroPedido;
        this.clienteId = clienteId;
        this.zonaId = zonaId;
        this.fechaEntrega = fechaEntrega;
        this.estado = estado;
        this.requiereRefrigeracion = requiereRefrigeracion;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getZonaId() { return zonaId; }
    public void setZonaId(String zonaId) { this.zonaId = zonaId; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public boolean isRequiereRefrigeracion() { return requiereRefrigeracion; }
    public void setRequiereRefrigeracion(boolean requiereRefrigeracion) { this.requiereRefrigeracion = requiereRefrigeracion; }
}
