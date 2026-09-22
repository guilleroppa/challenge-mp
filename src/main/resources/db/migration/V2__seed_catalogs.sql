-- V2__seed_catalogs.sql
-- Datos de ejemplo para pruebas locales

INSERT INTO clientes (id, activo) VALUES
    ('CLI-001', TRUE),
    ('CLI-002', TRUE),
    ('CLI-003', TRUE),
    ('CLI-100', TRUE),
    ('CLI-123', TRUE),
    ('CLI-456', TRUE),
    ('CLI-789', TRUE),
    ('CLI-999', TRUE),
    ('CLI-INACTIVO', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO zonas (id, soporte_refrigeracion) VALUES
    ('ZONA1', TRUE),
    ('ZONA2', TRUE),
    ('ZONA3', FALSE),
    ('ZONA4', FALSE),
    ('ZONA5', FALSE),
    ('ZONA6', TRUE)
ON CONFLICT (id) DO NOTHING;
