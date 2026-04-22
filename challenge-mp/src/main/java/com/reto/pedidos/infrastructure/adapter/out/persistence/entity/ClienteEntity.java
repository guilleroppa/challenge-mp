package com.reto.pedidos.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class ClienteEntity {

    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
