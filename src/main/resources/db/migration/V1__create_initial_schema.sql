-- V1__create_initial_schema.sql
-- Esquema inicial para el microservicio de pedidos

-- Tabla de clientes (catálogo)
CREATE TABLE clientes (
    id      VARCHAR(100) PRIMARY KEY,
    activo  BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla de zonas (catálogo)
CREATE TABLE zonas (
    id                    VARCHAR(100) PRIMARY KEY,
    soporte_refrigeracion BOOLEAN NOT NULL DEFAULT FALSE
);

-- Tabla de pedidos
CREATE TABLE pedidos (
    id                     UUID         PRIMARY KEY,
    numero_pedido          VARCHAR(100) NOT NULL,
    cliente_id             VARCHAR(100) NOT NULL,
    zona_id                VARCHAR(100) NOT NULL,
    fecha_entrega          DATE         NOT NULL,
    estado                 VARCHAR(20)  NOT NULL CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'ENTREGADO')),
    requiere_refrigeracion BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_numero_pedido UNIQUE (numero_pedido)
);

-- Índices en pedidos
CREATE INDEX idx_pedidos_estado_fecha ON pedidos (estado, fecha_entrega);

-- Tabla de idempotencia para cargas
CREATE TABLE cargas_idempotencia (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key  VARCHAR(255) NOT NULL,
    archivo_hash     VARCHAR(64)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_idempotency_key_hash UNIQUE (idempotency_key, archivo_hash)
);
