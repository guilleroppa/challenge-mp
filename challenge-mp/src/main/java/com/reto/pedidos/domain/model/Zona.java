package com.reto.pedidos.domain.model;

public class Zona {
    private String id;
    private boolean soporteRefrigeracion;

    public Zona(String id, boolean soporteRefrigeracion) {
        this.id = id;
        this.soporteRefrigeracion = soporteRefrigeracion;
    }

    public String getId() { return id; }
    public boolean isSoporteRefrigeracion() { return soporteRefrigeracion; }
}
