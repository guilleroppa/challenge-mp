package com.reto.pedidos.domain.model;

public class Cliente {
    private String id;
    private boolean activo;

    public Cliente(String id, boolean activo) {
        this.id = id;
        this.activo = activo;
    }

    public String getId() { return id; }
    public boolean isActivo() { return activo; }
}
