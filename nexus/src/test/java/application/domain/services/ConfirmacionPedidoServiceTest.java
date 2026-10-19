package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoPedido;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmacionPedidoServiceTest {

    private final ConfirmacionPedidoService servicio = new ConfirmacionPedidoService(
            new ValidacionCompradorService(),
            new ReservaInventarioService(),
            new LiberacionReservaService());

    private final Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);
    private final Comprador comprador = Fixtures.comprador(usuario);

    private Producto productoPublicado(TipoProducto tipo) {
        Producto producto = Fixtures.producto(tipo);
        producto.publicar();
        return producto;
    }

    @Test
    void confirmaElPedidoYReservaElStock() {
        Producto producto = productoPublicado(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(producto, 3);

        var resultado = servicio.confirmar(usuario, comprador, carrito, List.of(inventario));

        assertEquals(EstadoPedido.PendienteDePago, resultado.pedido().getEstado());
        assertEquals(1, resultado.reservas().size());
        assertEquals(1, resultado.movimientos().size());
        assertEquals(3, inventario.getReservado());
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void reservaCadaProductoDelCarrito() {
        Producto a = productoPublicado(TipoProducto.Fisico);
        Producto b = productoPublicado(TipoProducto.Fisico);
        Inventario invA = Fixtures.inventario(a, 10);
        Inventario invB = Fixtures.inventario(b, 10);
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(a, 2);
        carrito.agregarProducto(b, 4);

        var resultado = servicio.confirmar(usuario, comprador, carrito, List.of(invA, invB));

        assertEquals(2, resultado.reservas().size());
        assertEquals(2, invA.getReservado());
        assertEquals(4, invB.getReservado());
    }

    @Test
    void siFaltaStockNoDejaReservasPendientesNiVaciaElCarrito() {
        Producto a = productoPublicado(TipoProducto.Fisico);
        Producto b = productoPublicado(TipoProducto.Fisico);
        Inventario invA = Fixtures.inventario(a, 10);
        Inventario invB = Fixtures.inventario(b, 1);
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(a, 3);
        carrito.agregarProducto(b, 5);

        assertThrows(IllegalStateException.class,
                () -> servicio.confirmar(usuario, comprador, carrito, List.of(invA, invB)));

        assertEquals(0, invA.getReservado());
        assertEquals(0, invB.getReservado());
        assertEquals(2, carrito.getItems().size());
    }

    @Test
    void rechazaUnCarritoVacio() {
        Carrito carrito = new Carrito(comprador);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.confirmar(usuario, comprador, carrito, List.of()));
    }

    @Test
    void rechazaUnProductoQueYaNoEstaPublicado() {
        Producto producto = productoPublicado(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(producto, 2);
        producto.suspender();

        assertThrows(IllegalStateException.class,
                () -> servicio.confirmar(usuario, comprador, carrito, List.of(inventario)));
        assertEquals(0, inventario.getReservado());
    }

    @Test
    void unCompradorRestringidoNoPuedeConfirmar() {
        Producto producto = productoPublicado(TipoProducto.Digital);
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(producto, 1);
        comprador.restringir();

        assertThrows(IllegalStateException.class,
                () -> servicio.confirmar(usuario, comprador, carrito, List.of()));
    }

    @Test
    void losProductosDigitalesNoReservanInventario() {
        Producto digital = productoPublicado(TipoProducto.Digital);
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(digital, 2);

        var resultado = servicio.confirmar(usuario, comprador, carrito, List.of());

        assertTrue(resultado.reservas().isEmpty());
        assertTrue(resultado.pedido().esSoloDigital());
        assertTrue(carrito.getItems().isEmpty());
    }
}