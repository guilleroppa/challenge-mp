package com.reto.pedidos.domain.service;

import com.reto.pedidos.domain.model.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servicio de dominio puro: no depende de Spring ni de infraestructura.
 * Contiene todas las reglas de negocio para validar una fila CSV.
 */
public class PedidoValidadorService {

    private static final ZoneId ZONA_LIMA = ZoneId.of("America/Lima");
    private static final String ALFANUMERICO_REGEX = "^[a-zA-Z0-9-_]+$";

    /**
     * Valida una fila CSV contra los catálogos precargados.
     *
     * @param fila            la fila del CSV a validar
     * @param clientes        mapa de clientes por ID (ya cargados en batch)
     * @param zonas           mapa de zonas por ID (ya cargadas en batch)
     * @param numerosExistentes conjunto de numeroPedido ya existentes en BD
     * @param numerosEnLote   conjunto de numeroPedido vistos en el lote actual (para duplicados internos)
     * @return lista de errores encontrados (vacía si la fila es válida)
     */
    public List<ResultadoCarga.ErrorFila> validar(
            FilaPedidoCsv fila,
            Map<String, Cliente> clientes,
            Map<String, Zona> zonas,
            Set<String> numerosExistentes,
            Set<String> numerosEnLote) {

        List<ResultadoCarga.ErrorFila> errores = new ArrayList<>();

        // 1. Validar numeroPedido: alfanumérico y único
        if (fila.getNumeroPedido() == null || fila.getNumeroPedido().isBlank()
                || !fila.getNumeroPedido().matches(ALFANUMERICO_REGEX)) {
            errores.add(new ResultadoCarga.ErrorFila(
                    fila.getNumeroLinea(),
                    TipoError.NUMERO_PEDIDO_INVALIDO.name(),
                    "numeroPedido inválido o vacío: '" + fila.getNumeroPedido() + "'"));
        } else if (numerosExistentes.contains(fila.getNumeroPedido())
                || numerosEnLote.contains(fila.getNumeroPedido())) {
            errores.add(new ResultadoCarga.ErrorFila(
                    fila.getNumeroLinea(),
                    TipoError.DUPLICADO.name(),
                    "numeroPedido ya existe: " + fila.getNumeroPedido()));
        }

        // 2. Validar estado
        EstadoPedido estado = null;
        try {
            if (fila.getEstado() == null || fila.getEstado().isBlank()) throw new IllegalArgumentException();
            estado = EstadoPedido.valueOf(fila.getEstado().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            errores.add(new ResultadoCarga.ErrorFila(
                    fila.getNumeroLinea(),
                    TipoError.ESTADO_INVALIDO.name(),
                    "estado inválido: '" + fila.getEstado() + "'. Valores permitidos: PENDIENTE, CONFIRMADO, ENTREGADO"));
        }

        // 3. Validar fechaEntrega
        LocalDate fechaEntrega = null;
        try {
            if (fila.getFechaEntrega() == null || fila.getFechaEntrega().isBlank()) throw new IllegalArgumentException("vacío");
            fechaEntrega = LocalDate.parse(fila.getFechaEntrega().trim());
            LocalDate hoyLima = LocalDate.now(ZONA_LIMA);
            if (fechaEntrega.isBefore(hoyLima)) {
                errores.add(new ResultadoCarga.ErrorFila(
                        fila.getNumeroLinea(),
                        TipoError.FECHA_INVALIDA.name(),
                        "fechaEntrega " + fechaEntrega + " es pasada (hoy Lima: " + hoyLima + ")"));
                fechaEntrega = null; // marcar como inválida
            }
        } catch (Exception e) {
            errores.add(new ResultadoCarga.ErrorFila(
                    fila.getNumeroLinea(),
                    TipoError.FECHA_INVALIDA.name(),
                    "fechaEntrega inválida: '" + fila.getFechaEntrega() + "'. Formato esperado: yyyy-MM-dd"));
        }

        // 4. Validar clienteId
        if (fila.getClienteId() == null || fila.getClienteId().isBlank()) {
            errores.add(new ResultadoCarga.ErrorFila(
                    fila.getNumeroLinea(),
                    TipoError.CLIENTE_NO_ENCONTRADO.name(),
                    "clienteId vacío"));
        } else {
            Cliente cliente = clientes.get(fila.getClienteId().trim());
            if (cliente == null) {
                errores.add(new ResultadoCarga.ErrorFila(
                        fila.getNumeroLinea(),
                        TipoError.CLIENTE_NO_ENCONTRADO.name(),
                        "clienteId no encontrado: " + fila.getClienteId()));
            }
        }

        // 5. Validar zonaEntrega y refrigeración
        if (fila.getZonaEntrega() == null || fila.getZonaEntrega().isBlank()) {
            errores.add(new ResultadoCarga.ErrorFila(
                    fila.getNumeroLinea(),
                    TipoError.ZONA_INVALIDA.name(),
                    "zonaEntrega vacía"));
        } else {
            Zona zona = zonas.get(fila.getZonaEntrega().trim());
            if (zona == null) {
                errores.add(new ResultadoCarga.ErrorFila(
                        fila.getNumeroLinea(),
                        TipoError.ZONA_INVALIDA.name(),
                        "zonaEntrega no encontrada: " + fila.getZonaEntrega()));
            } else {
                boolean requiereRefri = Boolean.parseBoolean(
                        fila.getRequiereRefrigeracion() == null ? "false" : fila.getRequiereRefrigeracion().trim());
                if (requiereRefri && !zona.isSoporteRefrigeracion()) {
                    errores.add(new ResultadoCarga.ErrorFila(
                            fila.getNumeroLinea(),
                            TipoError.CADENA_FRIO_NO_SOPORTADA.name(),
                            "zona " + zona.getId() + " no soporta refrigeración"));
                }
            }
        }

        return errores;
    }

    /**
     * Convierte una fila válida en un objeto de dominio Pedido.
     */
    public Pedido toPedido(FilaPedidoCsv fila) {
        return new Pedido(
                fila.getNumeroPedido().trim(),
                fila.getClienteId().trim(),
                fila.getZonaEntrega().trim(),
                LocalDate.parse(fila.getFechaEntrega().trim()),
                EstadoPedido.valueOf(fila.getEstado().trim().toUpperCase()),
                Boolean.parseBoolean(fila.getRequiereRefrigeracion() == null ? "false" : fila.getRequiereRefrigeracion().trim())
        );
    }
}
