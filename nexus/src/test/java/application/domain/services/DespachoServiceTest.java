package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Envio;
import application.domain.entities.Inventario;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.entities.ReservaInventario;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoEnvio;
import application.domain.enums.EstadoPedido;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoMovimientoInventario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DespachoServiceTest {

    private record Escenario(Pedido pedido, Envio envio,
                             List<ReservaInventario> reservas, Inventario inventario) {
    }

    private final ConfirmacionPedidoService confirmacion = new ConfirmacionPedidoService(
            new ValidacionCompradorService(), new ReservaInventarioService(), new LiberacionReservaService());
    private final DespachoService servicio =
            new DespachoService(new AutorizacionService(), new SalidaInventarioService());

    private final Usuario operador = Fixtures.usuario(RolUsuario.OperadorLogistico);
    private final Usuario usuarioComprador = Fixtures.usuario(RolUsuario.Comprador);
    private final Comprador comprador = Fixtures.comprador(usuarioComprador);

    private Escenario pedidoPagadoConEnvio() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        producto.publicar();
        Inventario inventario = Fixtures.inventario(producto, 10);
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(producto, 3);

        var resultado = confirmacion.confirmar(usuarioComprador, comprador, carrito, List.of(inventario));
        resultado.pedido().pagar();
        Envio envio = new Envio(resultado.pedido(), comprador.getDireccionPrincipal());

        return new Escenario(resultado.pedido(), envio, resultado.reservas(), inventario);
    }

    @Test
    void despachaElPedidoYDescuentaElStock() {
        var e = pedidoPagadoConEnvio();

        var movimientos = servicio.despachar(operador, e.pedido(), e.envio(), e.reservas());

        assertEquals(EstadoPedido.Despachado, e.pedido().getEstado());
        assertEquals(EstadoEnvio.Despachado, e.envio().getEstado());
        assertEquals(7, e.inventario().getCantidad());
        assertEquals(0, e.inventario().getReservado());
        assertEquals(TipoMovimientoInventario.SalidaPorVenta, movimientos.get(0).getTipo());
    }

    @Test
    void soloElOperadorLogisticoDespacha() {
        var e = pedidoPagadoConEnvio();
        Usuario vendedor = Fixtures.usuario(RolUsuario.Vendedor);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.despachar(vendedor, e.pedido(), e.envio(), e.reservas()));
        assertEquals(EstadoPedido.Pagado, e.pedido().getEstado());
    }

    @Test
    void rechazaUnEnvioDeOtroPedido() {
        var a = pedidoPagadoConEnvio();
        var b = pedidoPagadoConEnvio();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.despachar(operador, a.pedido(), b.envio(), a.reservas()));
    }

    @Test
    void noSeDespachaDosVeces() {
        var e = pedidoPagadoConEnvio();
        servicio.despachar(operador, e.pedido(), e.envio(), e.reservas());

        assertThrows(IllegalStateException.class,
                () -> servicio.despachar(operador, e.pedido(), e.envio(), e.reservas()));
    }

    @Test
    void rechazaDespacharSinStockReservado() {
        var e = pedidoPagadoConEnvio();

        assertThrows(IllegalStateException.class,
                () -> servicio.despachar(operador, e.pedido(), e.envio(), List.of()));
        assertEquals(EstadoPedido.Pagado, e.pedido().getEstado());
        assertEquals(EstadoEnvio.Pendiente, e.envio().getEstado());
    }

    @Test
    void rechazaReservasDeOtroPedidoSinTocarNada() {
        var a = pedidoPagadoConEnvio();
        var b = pedidoPagadoConEnvio();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.despachar(operador, a.pedido(), a.envio(), b.reservas()));

        assertEquals(EstadoPedido.Pagado, a.pedido().getEstado());
        assertEquals(EstadoEnvio.Pendiente, a.envio().getEstado());
        assertEquals(3, b.inventario().getReservado());
    }
}