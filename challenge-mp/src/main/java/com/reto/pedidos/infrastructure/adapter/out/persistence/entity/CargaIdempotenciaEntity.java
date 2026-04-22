package com.reto.pedidos.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "cargas_idempotencia",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_idempotency_key_hash",
                columnNames = {"idempotency_key", "archivo_hash"})
    }
)
public class CargaIdempotenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "archivo_hash", nullable = false, length = 64)
    private String archivoHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CargaIdempotenciaEntity() {}

    public CargaIdempotenciaEntity(String idempotencyKey, String archivoHash) {
        this.idempotencyKey = idempotencyKey;
        this.archivoHash = archivoHash;
    }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getArchivoHash() { return archivoHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
