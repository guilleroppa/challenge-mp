package com.reto.pedidos.application.usecase;

import com.reto.pedidos.domain.model.*;
import com.reto.pedidos.domain.port.in.CargarPedidosUseCase;
import com.reto.pedidos.domain.port.out.*;
import com.reto.pedidos.domain.service.PedidoValidadorService;
import com.reto.pedidos.infrastructure.exception.IdempotenciaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CargarPedidosService implements CargarPedidosUseCase {

    private static final Logger log = LoggerFactory.getLogger(CargarPedidosService.class);

    private final PedidoValidadorService validador;
    private final PedidoRepositoryPort pedidoRepository;
    private final ClienteRepositoryPort clienteRepository;
    private final ZonaRepositoryPort zonaRepository;
    private final IdempotenciaRepositoryPort idempotenciaRepository;

    @Value("${pedidos.batch.size:500}")
    private int batchSize;

    public CargarPedidosService(PedidoValidadorService validador,
                                 PedidoRepositoryPort pedidoRepository,
                                 ClienteRepositoryPort clienteRepository,
                                 ZonaRepositoryPort zonaRepository,
                                 IdempotenciaRepositoryPort idempotenciaRepository) {
        this.validador = validador;
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.zonaRepository = zonaRepository;
        this.idempotenciaRepository = idempotenciaRepository;
    }

    @Override
    @Transactional
    public ResultadoCarga cargar(InputStream csv, String idempotencyKey) {
        // 1. Leer todos los bytes para calcular hash y procesar CSV
        byte[] bytes;
        try {
            bytes = csv.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo CSV", e);
        }

        String archivoHash = calcularHash(bytes);
        log.info("Iniciando carga. idempotencyKey={} hash={}", idempotencyKey, archivoHash);

        // 2. Verificar idempotencia
        if (idempotenciaRepository.existeClave(idempotencyKey, archivoHash)) {
            log.warn("Solicitud idempotente detectada. idempotencyKey={}", idempotencyKey);
            throw new IdempotenciaException("Solicitud ya procesada con Idempotency-Key: " + idempotencyKey);
        }

        // 3. Parsear todas las filas del CSV
        List<FilaPedidoCsv> filas = parsearCsv(bytes);
        log.info("Filas parseadas: {}", filas.size());

        ResultadoCarga resultado = new ResultadoCarga();
        Set<String> numerosEnLote = new HashSet<>();

        // 4. Procesar en batches
        int totalFilas = filas.size();
        for (int inicio = 0; inicio < totalFilas; inicio += batchSize) {
            int fin = Math.min(inicio + batchSize, totalFilas);
            List<FilaPedidoCsv> lote = filas.subList(inicio, fin);
            procesarLote(lote, resultado, numerosEnLote);
            log.debug("Lote procesado: filas {}-{}", inicio + 1, fin);
        }

        // 5. Registrar idempotencia
        idempotenciaRepository.registrarClave(idempotencyKey, archivoHash);

        log.info("Carga completada. total={} guardados={} errores={}",
                resultado.getTotalProcesados(), resultado.getGuardados(), resultado.getConError());
        return resultado;
    }

    private void procesarLote(List<FilaPedidoCsv> lote, ResultadoCarga resultado,
                               Set<String> numerosEnLote) {
        // Recolectar IDs únicos del lote para consultas batch
        Set<String> clienteIds = lote.stream()
                .map(FilaPedidoCsv::getClienteId)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> zonaIds = lote.stream()
                .map(FilaPedidoCsv::getZonaEntrega)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> numeros = lote.stream()
                .map(FilaPedidoCsv::getNumeroPedido)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        // Consultas batch a catálogos
        Map<String, Cliente> clientes = clienteRepository.buscarPorIds(clienteIds);
        Map<String, Zona> zonas = zonaRepository.buscarPorIds(zonaIds);
        Set<String> numerosExistentes = pedidoRepository.buscarNumerosPedidoExistentes(numeros);

        // Validar cada fila
        List<Pedido> pedidosValidos = new ArrayList<>();
        for (FilaPedidoCsv fila : lote) {
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);

            if (errores.isEmpty()) {
                Pedido pedido = validador.toPedido(fila);
                pedidosValidos.add(pedido);
                numerosEnLote.add(fila.getNumeroPedido().trim());
                resultado.incrementarGuardados();
            } else {
                errores.forEach(resultado::agregarError);
            }
        }

        // Inserción batch
        if (!pedidosValidos.isEmpty()) {
            pedidoRepository.guardarTodos(pedidosValidos);
        }
    }

    private List<FilaPedidoCsv> parsearCsv(byte[] bytes) {
        List<FilaPedidoCsv> filas = new ArrayList<>();
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(new java.io.ByteArrayInputStream(bytes), StandardCharsets.UTF_8))) {

            String[] header = reader.readNext(); // saltar cabecera
            if (header == null) return filas;

            String[] linea;
            int numeroLinea = 1;
            while ((linea = reader.readNext()) != null) {
                numeroLinea++;
                if (linea.length < 6) {
                    // fila malformada - se puede agregar como error genérico
                    continue;
                }
                filas.add(new FilaPedidoCsv(
                        numeroLinea,
                        linea[0], // numeroPedido
                        linea[1], // clienteId
                        linea[2], // fechaEntrega
                        linea[3], // estado
                        linea[4], // zonaEntrega
                        linea[5]  // requiereRefrigeracion
                ));
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Error parseando CSV", e);
        }
        return filas;
    }

    private String calcularHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }
}
