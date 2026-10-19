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
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfirmacionEntregaServiceTest {

    private record Escenario(Pedido pedido, Envio envio, List<ReservaInventario> reservas) {
    }

    private final ConfirmacionPedidoService confirmacion = new ConfirmacionPedidoService(
            new ValidacionCompradorService(), new ReservaInventarioService(), new LiberacionReservaService());
    private final DespachoService despacho =
            new DespachoService(new AutorizacionService(), new SalidaInventarioService());
    private final ConfirmacionEntregaService servicio =
            new ConfirmacionEntregaService(new AutorizacionService());

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

        return new Escenario(resultado.pedido(), envio, resultado.reservas());
    }

    private Escenario pedidoDespachado() {
        Escenario e = pedidoPagadoConEnvio();
        despacho.despachar(operador, e.pedido(), e.envio(), e.reservas());
        return e;
    }

    @Test
    void confirmaLaEntregaYFinalizaElPedido() {
        Escenario e = pedidoDespachado();

        servicio.confirmar(operador, e.pedido(), e.envio());

        assertEquals(EstadoEnvio.Entregado, e.envio().getEstado());
        assertEquals(EstadoPedido.Entregado, e.pedido().getEstado());
    }

    @Test
    void soloElOperadorLogisticoConfirmaLaEntrega() {
        Escenario e = pedidoDespachado();
        Usuario vendedor = Fixtures.usuario(RolUsuario.Vendedor);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.confirmar(vendedor, e.pedido(), e.envio()));
        assertEquals(EstadoPedido.Despachado, e.pedido().getEstado());
    }

    @Test
    void rechazaUnEnvioQueTodaviaNoFueDespachado() {
        Escenario e = pedidoPagadoConEnvio();

        assertThrows(IllegalStateException.class,
                () -> servicio.confirmar(operador, e.pedido(), e.envio()));
        assertEquals(EstadoPedido.Pagado, e.pedido().getEstado());
    }

    @Test
    void rechazaUnEnvioDeOtroPedido() {
        Escenario a = pedidoDespachado();
        Escenario b = pedidoDespachado();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.confirmar(operador, a.pedido(), b.envio()));
    }

    @Test
    void noSeConfirmaDosVeces() {
        Escenario e = pedidoDespachado();
        servicio.confirmar(operador, e.pedido(), e.envio());

        assertThrows(IllegalStateException.class,
                () -> servicio.confirmar(operador, e.pedido(), e.envio()));
    }
}