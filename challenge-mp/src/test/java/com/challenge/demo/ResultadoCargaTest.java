package com.challenge.demo;

import com.reto.pedidos.domain.model.ResultadoCarga;
import com.reto.pedidos.domain.model.TipoError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResultadoCarga - Acumulación de resultados")
class ResultadoCargaTest {

    @Test
    @DisplayName("Incrementar guardados actualiza totalProcesados y guardados")
    void incrementarGuardados_actualizaContadores() {
        ResultadoCarga resultado = new ResultadoCarga();
        resultado.incrementarGuardados();
        resultado.incrementarGuardados();

        assertThat(resultado.getTotalProcesados()).isEqualTo(2);
        assertThat(resultado.getGuardados()).isEqualTo(2);
        assertThat(resultado.getConError()).isEqualTo(0);
    }

    @Test
    @DisplayName("Agregar error actualiza totalProcesados y conError")
    void agregarError_actualizaContadores() {
        ResultadoCarga resultado = new ResultadoCarga();
        resultado.agregarError(new ResultadoCarga.ErrorFila(2, TipoError.DUPLICADO.name(), "Duplicado"));
        resultado.agregarError(new ResultadoCarga.ErrorFila(3, TipoError.ZONA_INVALIDA.name(), "Zona inválida"));

        assertThat(resultado.getTotalProcesados()).isEqualTo(2);
        assertThat(resultado.getConError()).isEqualTo(2);
        assertThat(resultado.getGuardados()).isEqualTo(0);
    }

    @Test
    @DisplayName("Errores agrupados por tipo correctamente")
    void erroresAgrupados_agrupaCorrectamentePorTipo() {
        ResultadoCarga resultado = new ResultadoCarga();
        resultado.agregarError(new ResultadoCarga.ErrorFila(2, TipoError.DUPLICADO.name(), "dup1"));
        resultado.agregarError(new ResultadoCarga.ErrorFila(3, TipoError.DUPLICADO.name(), "dup2"));
        resultado.agregarError(new ResultadoCarga.ErrorFila(4, TipoError.ZONA_INVALIDA.name(), "zona"));

        Map<String, ?> agrupados = resultado.getErroresAgrupados();
        assertThat(agrupados).containsKey(TipoError.DUPLICADO.name());
        assertThat(agrupados).containsKey(TipoError.ZONA_INVALIDA.name());
        assertThat((java.util.List<?>) agrupados.get(TipoError.DUPLICADO.name())).hasSize(2);
        assertThat((java.util.List<?>) agrupados.get(TipoError.ZONA_INVALIDA.name())).hasSize(1);
    }

    @Test
    @DisplayName("Resultado mixto: guardados y errores se acumulan correctamente")
    void resultadoMixto_contadoresCorrectos() {
        ResultadoCarga resultado = new ResultadoCarga();
        resultado.incrementarGuardados();
        resultado.incrementarGuardados();
        resultado.incrementarGuardados();
        resultado.agregarError(new ResultadoCarga.ErrorFila(5, TipoError.FECHA_INVALIDA.name(), "fecha"));

        assertThat(resultado.getTotalProcesados()).isEqualTo(4);
        assertThat(resultado.getGuardados()).isEqualTo(3);
        assertThat(resultado.getConError()).isEqualTo(1);
    }
}
