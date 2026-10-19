package application.domain.services;

import application.domain.entities.Devolucion;
import application.domain.entities.Reembolso;
import application.domain.enums.EstadoReembolso;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneracionReembolsoServiceTest {

    private final GeneracionReembolsoService servicio = new GeneracionReembolsoService();

    private Devolucion devolucion() {
        return new Devolucion(Fixtures.pedidoEntregado(Fixtures.producto(TipoProducto.Digital), 2),
                "Llego defectuoso");
    }

    @Test
    void generaElReembolsoPorElTotalDelPedido() {
        Devolucion devolucion = devolucion();
        devolucion.aprobar();

        Reembolso reembolso = servicio.generar(devolucion, List.of());

        assertEquals(EstadoReembolso.Pendiente, reembolso.getEstado());
        assertEquals(0, reembolso.getMonto().monto()
                .compareTo(devolucion.getPedido().calcularTotal().monto()));
    }

    @Test
    void rechazaUnaDevolucionQueSigueSolicitada() {
        Devolucion devolucion = devolucion();

        assertThrows(IllegalStateException.class, () -> servicio.generar(devolucion, List.of()));
    }

    @Test
    void rechazaUnaDevolucionRechazada() {
        Devolucion devolucion = devolucion();
        devolucion.rechazar();

        assertThrows(IllegalStateException.class, () -> servicio.generar(devolucion, List.of()));
    }

    @Test
    void noGeneraDosReembolsosParaElMismoPedido() {
        Devolucion devolucion = devolucion();
        devolucion.aprobar();
        Reembolso existente = servicio.generar(devolucion, List.of());

        assertThrows(IllegalStateException.class,
                () -> servicio.generar(devolucion, List.of(existente)));
    }
}