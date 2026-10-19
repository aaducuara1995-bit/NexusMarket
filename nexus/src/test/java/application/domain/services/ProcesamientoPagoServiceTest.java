package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoPedido;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import application.domain.valueobjects.Dinero;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcesamientoPagoServiceTest {

    private final ProcesamientoPagoService servicio =
            new ProcesamientoPagoService(new ValidacionCompradorService());

    private final Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);
    private final Comprador comprador = Fixtures.comprador(usuario);

    // Dos unidades de un producto de 45.000 COP: el total es 90.000 COP.
    private Pedido pedidoDeNoventaMil() {
        Producto producto = Fixtures.producto(TipoProducto.Digital);
        producto.publicar();
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(producto, 2);
        return new Pedido(carrito);
    }

    @Test
    void elCompradorPagaElTotalExacto() {
        Pedido pedido = pedidoDeNoventaMil();

        servicio.pagar(usuario, comprador, pedido, new Dinero(new BigDecimal("90000")));

        assertEquals(EstadoPedido.Pagado, pedido.getEstado());
    }

    @Test
    void rechazaUnMontoDistintoDelTotal() {
        Pedido pedido = pedidoDeNoventaMil();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.pagar(usuario, comprador, pedido, new Dinero(new BigDecimal("80000"))));
        assertEquals(EstadoPedido.PendienteDePago, pedido.getEstado());
    }

    @Test
    void rechazaUnPagoEnOtraMoneda() {
        Pedido pedido = pedidoDeNoventaMil();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.pagar(usuario, comprador, pedido, new Dinero(new BigDecimal("90000"), "USD")));
        assertEquals(EstadoPedido.PendienteDePago, pedido.getEstado());
    }

    @Test
    void elPedidoDebePertenecerAlCompradorQuePaga() {
        Pedido pedido = pedidoDeNoventaMil();
        Usuario otroUsuario = Fixtures.usuario(RolUsuario.Comprador);
        Comprador otro = Fixtures.comprador(otroUsuario);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.pagar(otroUsuario, otro, pedido, new Dinero(new BigDecimal("90000"))));
    }

    @Test
    void unPedidoNoSePagaDosVeces() {
        Pedido pedido = pedidoDeNoventaMil();
        servicio.pagar(usuario, comprador, pedido, new Dinero(new BigDecimal("90000")));

        assertThrows(IllegalStateException.class,
                () -> servicio.pagar(usuario, comprador, pedido, new Dinero(new BigDecimal("90000"))));
    }
}