package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.enums.TipoMovimientoInventario;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservaInventarioServiceTest {

    private final ReservaInventarioService servicio = new ReservaInventarioService();

    @Test
    void reservaDesdeUnaSolaBodega() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);

        var resultado = servicio.reservar(UUID.randomUUID(), producto, 4, List.of(inventario));

        assertEquals(1, resultado.reservas().size());
        assertEquals(4, inventario.getReservado());
        assertEquals(TipoMovimientoInventario.Reserva, resultado.movimientos().get(0).getTipo());
        assertEquals(4, resultado.movimientos().get(0).getCantidad());
    }

    @Test
    void repartePedidoEntreVariasBodegasEmpezandoPorLaQueMasTiene() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario a = Fixtures.inventario(producto, 3);
        Inventario b = Fixtures.inventario(producto, 5);

        var resultado = servicio.reservar(UUID.randomUUID(), producto, 6, List.of(a, b));

        assertEquals(2, resultado.reservas().size());
        assertEquals(5, b.getReservado());
        assertEquals(1, a.getReservado());
    }

    @Test
    void noReservaNadaSiNoAlcanzanLasExistencias() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario a = Fixtures.inventario(producto, 3);
        Inventario b = Fixtures.inventario(producto, 2);

        assertThrows(IllegalStateException.class,
                () -> servicio.reservar(UUID.randomUUID(), producto, 6, List.of(a, b)));

        assertEquals(0, a.getReservado());
        assertEquals(0, b.getReservado());
    }

    @Test
    void noUsaInventarioDanado() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario danado = Fixtures.inventario(producto, 10);
        danado.marcarDanado();
        Inventario bueno = Fixtures.inventario(producto, 4);

        assertThrows(IllegalStateException.class,
                () -> servicio.reservar(UUID.randomUUID(), producto, 5, List.of(danado, bueno)));
    }

    @Test
    void unProductoDigitalNoReservaNada() {
        Producto digital = Fixtures.producto(TipoProducto.Digital);

        var resultado = servicio.reservar(UUID.randomUUID(), digital, 3, List.of());

        assertTrue(resultado.reservas().isEmpty());
        assertTrue(resultado.movimientos().isEmpty());
    }

    @Test
    void rechazaCantidadesInvalidas() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.reservar(UUID.randomUUID(), producto, 0, List.of()));
    }
}