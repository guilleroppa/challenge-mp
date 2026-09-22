package com.reto.pedidos.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resultado del procesamiento del archivo CSV de pedidos")
public record ResultadoCargaResponse(

        @Schema(description = "Total de filas procesadas en el archivo")
        int totalProcesados,

        @Schema(description = "Pedidos guardados exitosamente")
        int guardados,

        @Schema(description = "Filas con errores de validación")
        int conError,

        @Schema(description = "Detalle de errores por fila")
        List<ErrorFilaResponse> errores,

        @Schema(description = "Errores agrupados por tipo")
        Map<String, List<ErrorFilaResponse>> erroresAgrupados
) {
    @Schema(description = "Error de validación en una fila específica")
    public record ErrorFilaResponse(
            @Schema(description = "Número de línea en el CSV (incluye cabecera)")
            int numeroLinea,
            @Schema(description = "Tipo de error")
            String tipoError,
            @Schema(description = "Descripción del motivo del error")
            String motivo
    ) {}
}
