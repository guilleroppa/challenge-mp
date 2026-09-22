package com.reto.pedidos.infrastructure.adapter.in.rest.mapper;

import com.reto.pedidos.domain.model.ResultadoCarga;
import com.reto.pedidos.infrastructure.adapter.in.rest.dto.ResultadoCargaResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ResultadoCargaResponseMapper {

    public ResultadoCargaResponse toResponse(ResultadoCarga resultado) {
        List<ResultadoCargaResponse.ErrorFilaResponse> errores = resultado.getErrores().stream()
                .map(e -> new ResultadoCargaResponse.ErrorFilaResponse(
                        e.numeroLinea(), e.tipoError(), e.motivo()))
                .toList();

        Map<String, List<ResultadoCargaResponse.ErrorFilaResponse>> agrupados =
                resultado.getErroresAgrupados().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().stream()
                                        .map(e -> new ResultadoCargaResponse.ErrorFilaResponse(
                                                e.numeroLinea(), e.tipoError(), e.motivo()))
                                        .toList()
                        ));

        return new ResultadoCargaResponse(
                resultado.getTotalProcesados(),
                resultado.getGuardados(),
                resultado.getConError(),
                errores,
                agrupados
        );
    }
}
