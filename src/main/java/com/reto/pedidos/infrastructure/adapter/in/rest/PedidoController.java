package com.reto.pedidos.infrastructure.adapter.in.rest;

import com.reto.pedidos.domain.model.ResultadoCarga;
import com.reto.pedidos.domain.port.in.CargarPedidosUseCase;
import com.reto.pedidos.infrastructure.adapter.in.rest.dto.ApiErrorResponse;
import com.reto.pedidos.infrastructure.adapter.in.rest.dto.ResultadoCargaResponse;
import com.reto.pedidos.infrastructure.adapter.in.rest.mapper.ResultadoCargaResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "API para carga de pedidos desde archivos CSV")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private static final Logger log = LoggerFactory.getLogger(PedidoController.class);

    private final CargarPedidosUseCase cargarPedidosUseCase;
    private final ResultadoCargaResponseMapper responseMapper;

    public PedidoController(CargarPedidosUseCase cargarPedidosUseCase,
                             ResultadoCargaResponseMapper responseMapper) {
        this.cargarPedidosUseCase = cargarPedidosUseCase;
        this.responseMapper = responseMapper;
    }

    @PostMapping(value = "/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Cargar pedidos desde CSV",
            description = "Recibe un archivo CSV con pedidos, los valida y persiste los válidos. " +
                    "Requiere header Idempotency-Key para evitar reprocesamiento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Procesamiento completado",
                    content = @Content(schema = @Schema(implementation = ResultadoCargaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o cabecera faltante",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "Solicitud ya procesada (idempotencia)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ResultadoCargaResponse> cargarPedidos(
            @Parameter(description = "Clave única por solicitud para idempotencia", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,

            @Parameter(description = "Archivo CSV con pedidos (UTF-8, delimitador coma)", required = true)
            @RequestPart("file") MultipartFile file) throws IOException {

        log.info("Recibida solicitud de carga. idempotencyKey={} filename={} size={}",
                idempotencyKey, file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ResultadoCarga resultado = cargarPedidosUseCase.cargar(file.getInputStream(), idempotencyKey);
        ResultadoCargaResponse response = responseMapper.toResponse(resultado);

        log.info("Carga finalizada. total={} guardados={} errores={}",
                response.totalProcesados(), response.guardados(), response.conError());

        return ResponseEntity.ok(response);
    }
}
