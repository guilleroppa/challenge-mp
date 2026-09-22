# Pedidos Service

Microservicio para carga de pedidos desde archivos CSV. Construido con **Java 17**, **Spring Boot 3**, **arquitectura hexagonal** y procesamiento eficiente por lotes (batch).

---

## Tabla de contenidos

- [Requisitos previos](#requisitos-previos)
- [Ejecución local rápida (perfil dev)](#ejecución-local-rápida-perfil-dev)
- [Ejecución con OAuth2 completo](#ejecución-con-oauth2-completo)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Decisiones de diseño](#decisiones-de-diseño)
- [Estrategia de batch](#estrategia-de-batch)
- [Idempotencia](#idempotencia)
- [API Reference](#api-reference)
- [Pruebas](#pruebas)
- [Supuestos](#supuestos)
- [Límites conocidos](#límites-conocidos)

---

## Requisitos previos

| Herramienta | Versión mínima |
|-------------|----------------|
| Java JDK    | 17             |
| Maven       | 3.9+           |
| Docker      | 24+            |
| Docker Compose | 2.x         |

---

## Ejecución local rápida (perfil dev)

Este modo **no requiere Keycloak**. Ideal para probar la funcionalidad del CSV.

### 1. Levantar PostgreSQL

```bash
docker compose up postgres -d
```

### 2. Ejecutar la aplicación

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

O con Maven instalado:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

La app arranca en `http://localhost:8081`.

### 3. Probar el endpoint

```bash
curl -X POST http://localhost:8081/pedidos/cargar \
  -H "Idempotency-Key: mi-clave-unica-001" \
  -F "file=@samples/pedidos_muestra.csv"
```

### 4. Ver Swagger UI

Abre en el navegador: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

---

## Ejecución con OAuth2 completo

### 1. Levantar todos los servicios

```bash
docker compose up postgres keycloak -d
```

### 2. Configurar realm en Keycloak

1. Accede a `http://localhost:8080` (admin/admin)
2. Crea un realm llamado `pedidos`
3. Crea un client `pedidos-client` de tipo `confidential`
4. Obtén un token:

```bash
curl -X POST http://localhost:8080/realms/pedidos/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=pedidos-client&client_secret=TU_SECRET"
```

### 3. Usar el token en las requests

```bash
curl -X POST http://localhost:8081/pedidos/cargar \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Idempotency-Key: mi-clave-unica-001" \
  -F "file=@samples/pedidos_muestra.csv"
```

---

## Estructura del proyecto

```
src/main/java/com/reto/pedidos/
├── domain/                          # ← Núcleo de negocio (sin dependencias externas)
│   ├── model/                       # Entidades y value objects del dominio
│   │   ├── Pedido.java
│   │   ├── Cliente.java
│   │   ├── Zona.java
│   │   ├── EstadoPedido.java
│   │   ├── FilaPedidoCsv.java
│   │   ├── ResultadoCarga.java
│   │   └── TipoError.java
│   ├── port/
│   │   ├── in/                      # Puertos de entrada (casos de uso)
│   │   │   └── CargarPedidosUseCase.java
│   │   └── out/                     # Puertos de salida (contratos con infraestructura)
│   │       ├── PedidoRepositoryPort.java
│   │       ├── ClienteRepositoryPort.java
│   │       ├── ZonaRepositoryPort.java
│   │       └── IdempotenciaRepositoryPort.java
│   └── service/                     # Servicios de dominio puros
│       └── PedidoValidadorService.java
│
├── application/                     # ← Orquestación de casos de uso
│   └── usecase/
│       └── CargarPedidosService.java
│
└── infrastructure/                  # ← Adaptadores (detalles técnicos)
    ├── adapter/
    │   ├── in/rest/                 # Adaptador REST (entrada)
    │   │   ├── PedidoController.java
    │   │   ├── dto/
    │   │   └── mapper/
    │   └── out/persistence/         # Adaptador JPA (salida)
    │       ├── entity/
    │       ├── repository/
    │       ├── mapper/
    │       └── *PersistenceAdapter.java
    ├── config/                      # Configuración de infraestructura
    └── exception/
```

---

## Decisiones de diseño

### Arquitectura Hexagonal (Ports & Adapters)

- El **dominio** no depende de Spring, JPA ni ningún framework. Es POJO puro.
- Los **puertos de entrada** (`port/in`) definen contratos para los casos de uso.
- Los **puertos de salida** (`port/out`) definen contratos para la persistencia.
- Los **adaptadores** implementan esos contratos usando tecnologías concretas.

Esto permite testear el dominio de forma completamente aislada y reemplazar infraestructura sin tocar la lógica de negocio.

### Separación de responsabilidades

| Capa | Responsabilidad |
|------|----------------|
| `PedidoValidadorService` | Validaciones de negocio puras (sin I/O) |
| `CargarPedidosService` | Orquestación: parseo CSV → batches → validación → persistencia |
| `PedidoController` | HTTP: deserializar request, delegar al caso de uso, serializar response |
| `*PersistenceAdapter` | Traducir entre modelo de dominio y entidades JPA |

### Manejo de errores estándar

Todas las respuestas de error siguen el modelo: `{ code, message, details[], correlationId }`.

---

## Estrategia de batch

El procesamiento del CSV se realiza en **lotes de tamaño configurable** (por defecto 500, rango 500–1000):

```
CSV completo (hasta 100.000 filas)
        │
        ▼
  Parsear todas las filas en memoria (FilaPedidoCsv[])
        │
        ▼ por cada lote de N filas
  ┌─────────────────────────────────────────────┐
  │  1. Recolectar IDs únicos del lote           │
  │  2. Consulta batch a clientes (IN (...))     │
  │  3. Consulta batch a zonas (IN (...))        │
  │  4. Consulta batch a pedidos existentes      │
  │  5. Validar cada fila contra catálogos       │
  │  6. saveAll() + flush() de válidos           │
  └─────────────────────────────────────────────┘
        │
        ▼
  Registrar idempotencia
```

**Ventajas:**
- Se hacen **3 consultas batch por lote** en lugar de N×3 consultas individuales.
- `saveAll()` + `jdbc.batch_size=500` en Hibernate usa JDBC batch internamente.
- El tamaño de lote es configurable via `BATCH_SIZE` env var o `pedidos.batch.size` en `application.yml`.

---

## Idempotencia

El endpoint requiere el header `Idempotency-Key` en cada request.

**Flujo:**
1. Se calcula el hash **SHA-256** del archivo recibido.
2. Se consulta la tabla `cargas_idempotencia` buscando la combinación `(idempotency_key, archivo_hash)`.
3. Si existe → se retorna **HTTP 409 Conflict** sin reprocesar.
4. Si no existe → se procesa normalmente y se registra la clave al finalizar.

Esto garantiza que el mismo archivo enviado con la misma clave solo se procese una vez, aunque la request se repita por errores de red.

---

## API Reference

### `POST /pedidos/cargar`

**Headers:**

| Header | Requerido | Descripción |
|--------|-----------|-------------|
| `Authorization` | Sí | `Bearer <JWT>` |
| `Idempotency-Key` | Sí | Identificador único de la solicitud |
| `X-Correlation-Id` | No | Si no se envía, se genera automáticamente |

**Body:** `multipart/form-data` con campo `file` (CSV UTF-8).

**Respuesta 200:**

```json
{
  "totalProcesados": 10,
  "guardados": 5,
  "conError": 5,
  "errores": [
    { "numeroLinea": 7, "tipoError": "CLIENTE_NO_ENCONTRADO", "motivo": "clienteId no encontrado: CLI-DESCONOCIDO" },
    { "numeroLinea": 8, "tipoError": "FECHA_INVALIDA", "motivo": "fechaEntrega 2020-01-01 es pasada" }
  ],
  "erroresAgrupados": {
    "CLIENTE_NO_ENCONTRADO": [...],
    "FECHA_INVALIDA": [...]
  }
}
```

**Tipos de error:**

| Código | Causa |
|--------|-------|
| `NUMERO_PEDIDO_INVALIDO` | No alfanumérico o vacío |
| `DUPLICADO` | Ya existe en BD o en el mismo lote |
| `CLIENTE_NO_ENCONTRADO` | clienteId no existe en tabla clientes |
| `ZONA_INVALIDA` | zonaEntrega no existe en tabla zonas |
| `FECHA_INVALIDA` | Fecha pasada o formato incorrecto |
| `ESTADO_INVALIDO` | Valor fuera de PENDIENTE/CONFIRMADO/ENTREGADO |
| `CADENA_FRIO_NO_SOPORTADA` | requiereRefrigeracion=true pero zona no soporta |

---

## Pruebas

### Ejecutar tests unitarios

```bash
mvn test
```

### Ejecutar con reporte de cobertura

```bash
mvn verify
# Reporte en: target/site/jacoco/index.html
```

### Colección Postman

Importar `postman/pedidos-collection.json` en Postman.

1. Ajustar la variable `base_url` si es necesario.
2. En perfil `dev` no se necesita token.
3. Ejecutar las requests en orden para probar idempotencia.

---

## Supuestos

1. **Zona horaria Lima:** La validación de fecha pasada usa `ZoneId.of("America/Lima")` según el enunciado.
2. **Cliente activo:** El enunciado solo requiere que `clienteId` exista en la tabla. No se valida el campo `activo`, aunque la columna existe para uso futuro.
3. **Formato CSV:** Se asume UTF-8 estrictamente. Archivos con otra codificación pueden producir resultados inesperados.
4. **Columnas del CSV:** Se esperan exactamente 6 columnas en el orden definido. Filas con menos columnas se descartan silenciosamente.
5. **Datos de catálogos:** El script `V2__seed_catalogs.sql` carga datos de prueba. En producción los catálogos se gestionan por separado.

---

## Límites conocidos

| Limitación | Impacto | Mitigación futura |
|------------|---------|-------------------|
| Parseo completo en memoria | Para archivos de 100k filas puede usar ~200MB heap | Streaming con OpenCSV en modo iterador |
| Sin caché de catálogos | 3 queries DB por lote | Agregar Caffeine cache con TTL corto |
| Sin reintentos en fallo de BD | Un error a mitad de lote pierde ese lote | Implementar retry con `@Retryable` |
| Keycloak manual para pruebas | Configuración inicial costosa | Agregar Testcontainers con Keycloak para tests de integración |
