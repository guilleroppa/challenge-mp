package com.reto.pedidos.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "zonas")
public class ZonaEntity {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "soporte_refrigeracion", nullable = false)
    private boolean soporteRefrigeracion;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isSoporteRefrigeracion() { return soporteRefrigeracion; }
    public void setSoporteRefrigeracion(boolean soporteRefrigeracion) { this.soporteRefrigeracion = soporteRefrigeracion; }
}
