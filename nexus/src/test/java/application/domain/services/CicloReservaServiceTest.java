package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.enums.EstadoReserva;
import application.domain.enums.TipoMovimientoInventario;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CicloReservaServiceTest {

    private final ReservaInventarioService reservaService = new ReservaInventarioService();
    private final LiberacionReservaService liberacionService = new LiberacionReservaService();
    private final SalidaInventarioService salidaService = new SalidaInventarioService();

    @Test
    void liberarDevuelveLasUnidadesAlDisponible() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        var reservas = reservaService.reservar(UUID.randomUUID(), producto, 4, List.of(inventario)).reservas();

        var movimientos = liberacionService.liberar(reservas);

        assertEquals(0, inventario.getReservado());
        assertEquals(10, inventario.getDisponible());
        assertEquals(EstadoReserva.Liberada, reservas.get(0).getEstado());
        assertEquals(-4, movimientos.get(0).getCantidad());
        assertEquals(TipoMovimientoInventario.Reserva, movimientos.get(0).getTipo());
    }

    @Test
    void unaReservaNoSePuedeLiberarDosVeces() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        var reservas = reservaService.reservar(UUID.randomUUID(), producto, 4, List.of(inventario)).reservas();
        liberacionService.liberar(reservas);

        assertThrows(IllegalStateException.class, () -> liberacionService.liberar(reservas));
    }

    @Test
    void confirmarSalidaDescuentaElStockDefinitivamente() {
        UUID pedidoId = UUID.randomUUID();
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        var reservas = reservaService.reservar(pedidoId, producto, 4, List.of(inventario)).reservas();

        var movimientos = salidaService.confirmarSalida(pedidoId, reservas);

        assertEquals(6, inventario.getCantidad());
        assertEquals(0, inventario.getReservado());
        assertEquals(EstadoReserva.Confirmada, reservas.get(0).getEstado());
        assertEquals(TipoMovimientoInventario.SalidaPorVenta, movimientos.get(0).getTipo());
        assertEquals(-4, movimientos.get(0).getCantidad());
    }

    @Test
    void laSalidaRechazaReservasDeOtroPedido() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        var reservas = reservaService.reservar(UUID.randomUUID(), producto, 4, List.of(inventario)).reservas();

        assertThrows(IllegalArgumentException.class,
                () -> salidaService.confirmarSalida(UUID.randomUUID(), reservas));
        assertEquals(4, inventario.getReservado());
    }
}