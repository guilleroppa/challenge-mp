package com.reto.pedidos.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Modelo estándar de error")
public record ApiErrorResponse(
        @Schema(description = "Código de error") String code,
        @Schema(description = "Mensaje descriptivo") String message,
        @Schema(description = "Detalles adicionales") List<String> details,
        @Schema(description = "ID de correlación del request") String correlationId
) {}
