package com.reto.pedidos.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResultadoCarga {

    private int totalProcesados;
    private int guardados;
    private int conError;
    private List<ErrorFila> errores = new ArrayList<>();

    public void incrementarGuardados() {
        this.guardados++;
        this.totalProcesados++;
    }

    public void agregarError(ErrorFila error) {
        this.errores.add(error);
        this.conError++;
        this.totalProcesados++;
    }

    public Map<String, List<ErrorFila>> getErroresAgrupados() {
        return errores.stream().collect(Collectors.groupingBy(ErrorFila::getTipoError));
    }

    public int getTotalProcesados() { return totalProcesados; }
    public int getGuardados() { return guardados; }
    public int getConError() { return conError; }
    public List<ErrorFila> getErrores() { return errores; }

    public record ErrorFila(int numeroLinea, String tipoError, String motivo) {
        public String getTipoError() { return tipoError; }
    }
}
