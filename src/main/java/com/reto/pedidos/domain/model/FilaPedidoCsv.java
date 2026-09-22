package com.reto.pedidos.domain.model;

public class FilaPedidoCsv {

    private final int numeroLinea;
    private final String numeroPedido;
    private final String clienteId;
    private final String fechaEntrega;
    private final String estado;
    private final String zonaEntrega;
    private final String requiereRefrigeracion;

    public FilaPedidoCsv(int numeroLinea, String numeroPedido, String clienteId,
                         String fechaEntrega, String estado, String zonaEntrega,
                         String requiereRefrigeracion) {
        this.numeroLinea = numeroLinea;
        this.numeroPedido = numeroPedido;
        this.clienteId = clienteId;
        this.fechaEntrega = fechaEntrega;
        this.estado = estado;
        this.zonaEntrega = zonaEntrega;
        this.requiereRefrigeracion = requiereRefrigeracion;
    }

    public int getNumeroLinea() { return numeroLinea; }
    public String getNumeroPedido() { return numeroPedido; }
    public String getClienteId() { return clienteId; }
    public String getFechaEntrega() { return fechaEntrega; }
    public String getEstado() { return estado; }
    public String getZonaEntrega() { return zonaEntrega; }
    public String getRequiereRefrigeracion() { return requiereRefrigeracion; }
}
