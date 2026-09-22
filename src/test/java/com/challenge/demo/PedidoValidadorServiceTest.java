package com.challenge.demo;

import com.reto.pedidos.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PedidoValidadorService - Reglas de negocio")
class PedidoValidadorServiceTest {

    private PedidoValidadorService validador;

    private Map<String, Cliente> clientes;
    private Map<String, Zona> zonas;
    private Set<String> numerosExistentes;
    private Set<String> numerosEnLote;

    private static final String FECHA_FUTURA = LocalDate.now(ZoneId.of("America/Lima"))
            .plusDays(5).toString();
    private static final String FECHA_PASADA = LocalDate.now(ZoneId.of("America/Lima"))
            .minusDays(1).toString();

    @BeforeEach
    void setUp() {
        validador = new PedidoValidadorService();

        clientes = new HashMap<>();
        clientes.put("CLI-123", new Cliente("CLI-123", true));
        clientes.put("CLI-INACTIVO", new Cliente("CLI-INACTIVO", false));

        zonas = new HashMap<>();
        zonas.put("ZONA1", new Zona("ZONA1", true));
        zonas.put("ZONA3", new Zona("ZONA3", false));

        numerosExistentes = new HashSet<>();
        numerosEnLote = new HashSet<>();
    }

    @Nested
    @DisplayName("Validación exitosa")
    class ValidacionExitosa {

        @Test
        @DisplayName("Fila completamente válida sin refrigeración no produce errores")
        void filaValida_sinRefrigeracion_noTieneErrores() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).isEmpty();
        }

        @Test
        @DisplayName("Fila válida con refrigeración en zona que soporta cold chain")
        void filaValida_conRefrigeracion_zonaSoporta_noTieneErrores() {
            FilaPedidoCsv fila = fila("P002", "CLI-123", FECHA_FUTURA, "CONFIRMADO", "ZONA1", "true");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).isEmpty();
        }

        @Test
        @DisplayName("Estado ENTREGADO es válido")
        void estadoEntregado_esValido() {
            FilaPedidoCsv fila = fila("P003", "CLI-123", FECHA_FUTURA, "ENTREGADO", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).isEmpty();
        }
    }

    @Nested
    @DisplayName("Validación de numeroPedido")
    class ValidacionNumeroPedido {

        @Test
        @DisplayName("numeroPedido nulo genera error NUMERO_PEDIDO_INVALIDO")
        void numeroPedido_nulo_generaError() {
            FilaPedidoCsv fila = fila(null, "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.NUMERO_PEDIDO_INVALIDO.name()));
        }

        @Test
        @DisplayName("numeroPedido con caracteres especiales genera error")
        void numeroPedido_caracteresEspeciales_generaError() {
            FilaPedidoCsv fila = fila("P001 !", "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.NUMERO_PEDIDO_INVALIDO.name()));
        }

        @Test
        @DisplayName("numeroPedido duplicado en BD genera error DUPLICADO")
        void numeroPedido_existeEnBD_generaErrorDuplicado() {
            numerosExistentes.add("P001");
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.DUPLICADO.name()));
        }

        @Test
        @DisplayName("numeroPedido duplicado en el mismo lote genera error DUPLICADO")
        void numeroPedido_duplicadoEnLote_generaErrorDuplicado() {
            numerosEnLote.add("P001");
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.DUPLICADO.name()));
        }
    }

    @Nested
    @DisplayName("Validación de estado")
    class ValidacionEstado {

        @Test
        @DisplayName("Estado inválido genera error ESTADO_INVALIDO")
        void estadoInvalido_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "ANULADO", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.ESTADO_INVALIDO.name()));
        }

        @Test
        @DisplayName("Estado vacío genera error ESTADO_INVALIDO")
        void estadoVacio_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.ESTADO_INVALIDO.name()));
        }

        @Test
        @DisplayName("Estado en minúsculas es aceptado (case-insensitive)")
        void estadoMinusculas_esAceptado() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "pendiente", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).noneMatch(e -> e.tipoError().equals(TipoError.ESTADO_INVALIDO.name()));
        }
    }

    @Nested
    @DisplayName("Validación de fechaEntrega")
    class ValidacionFecha {

        @Test
        @DisplayName("Fecha pasada genera error FECHA_INVALIDA")
        void fechaPasada_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_PASADA, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.FECHA_INVALIDA.name()));
        }

        @Test
        @DisplayName("Fecha con formato incorrecto genera error FECHA_INVALIDA")
        void fechaFormatoIncorrecto_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", "10/08/2099", "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.FECHA_INVALIDA.name()));
        }

        @Test
        @DisplayName("Fecha nula genera error FECHA_INVALIDA")
        void fechaNula_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", null, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.FECHA_INVALIDA.name()));
        }
    }

    @Nested
    @DisplayName("Validación de cliente")
    class ValidacionCliente {

        @Test
        @DisplayName("Cliente inexistente genera error CLIENTE_NO_ENCONTRADO")
        void clienteInexistente_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-DESCONOCIDO", FECHA_FUTURA, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.CLIENTE_NO_ENCONTRADO.name()));
        }

        @Test
        @DisplayName("clienteId vacío genera error CLIENTE_NO_ENCONTRADO")
        void clienteIdVacio_generaError() {
            FilaPedidoCsv fila = fila("P001", "", FECHA_FUTURA, "PENDIENTE", "ZONA1", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.CLIENTE_NO_ENCONTRADO.name()));
        }
    }

    @Nested
    @DisplayName("Validación de zona y refrigeración")
    class ValidacionZona {

        @Test
        @DisplayName("Zona inexistente genera error ZONA_INVALIDA")
        void zonaInexistente_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA99", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.ZONA_INVALIDA.name()));
        }

        @Test
        @DisplayName("Zona sin soporte de refrigeración con requiereRefrigeracion=true genera error")
        void zonaSinRefrigeracion_conRequiereRefrigeracion_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA3", "true");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.CADENA_FRIO_NO_SOPORTADA.name()));
        }

        @Test
        @DisplayName("Zona sin soporte de refrigeración con requiereRefrigeracion=false es válida")
        void zonaSinRefrigeracion_sinRequiereRefrigeracion_esValida() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA3", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).noneMatch(e -> e.tipoError().equals(TipoError.CADENA_FRIO_NO_SOPORTADA.name()));
        }

        @Test
        @DisplayName("zonaEntrega vacía genera error ZONA_INVALIDA")
        void zonaVacia_generaError() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "PENDIENTE", "", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).anyMatch(e -> e.tipoError().equals(TipoError.ZONA_INVALIDA.name()));
        }
    }

    @Nested
    @DisplayName("Múltiples errores en una fila")
    class MultiplesErrores {

        @Test
        @DisplayName("Fila con varios campos inválidos genera múltiples errores")
        void filaConVariosErrores_generaMultiplesErrores() {
            FilaPedidoCsv fila = fila("P001 !", "CLI-DESCONOCIDO", FECHA_PASADA, "INVALIDO", "ZONA99", "false");
            List<ResultadoCarga.ErrorFila> errores = validador.validar(
                    fila, clientes, zonas, numerosExistentes, numerosEnLote);
            assertThat(errores).hasSizeGreaterThanOrEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Conversión a dominio Pedido")
    class ConversionPedido {

        @Test
        @DisplayName("toPedido convierte correctamente una fila válida")
        void toPedido_filaValida_convierteCorrectamente() {
            FilaPedidoCsv fila = fila("P001", "CLI-123", FECHA_FUTURA, "PENDIENTE", "ZONA1", "true");
            Pedido pedido = validador.toPedido(fila);

            assertThat(pedido.getNumeroPedido()).isEqualTo("P001");
            assertThat(pedido.getClienteId()).isEqualTo("CLI-123");
            assertThat(pedido.getZonaId()).isEqualTo("ZONA1");
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
            assertThat(pedido.isRequiereRefrigeracion()).isTrue();
            assertThat(pedido.getFechaEntrega()).isEqualTo(LocalDate.parse(FECHA_FUTURA));
            assertThat(pedido.getId()).isNotNull();
        }
    }

    // Helper
    private FilaPedidoCsv fila(String numeroPedido, String clienteId, String fechaEntrega,
                                String estado, String zonaEntrega, String requiereRefrigeracion) {
        return new FilaPedidoCsv(2, numeroPedido, clienteId, fechaEntrega,
                estado, zonaEntrega, requiereRefrigeracion);
    }
}
