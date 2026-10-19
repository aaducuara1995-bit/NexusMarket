package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.enums.EstadoPedido;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntregaDigitalServiceTest {

    private final EntregaDigitalService servicio = new EntregaDigitalService();

    private Pedido pedidoDe(TipoProducto tipo) {
        Producto producto = Fixtures.producto(tipo);
        producto.publicar();
        Carrito carrito = new Carrito(Fixtures.comprador(Fixtures.usuario(RolUsuario.Comprador)));
        carrito.agregarProducto(producto, 1);
        return new Pedido(carrito);
    }

    @Test
    void entregaUnPedidoSoloDigitalYaPagado() {
        Pedido pedido = pedidoDe(TipoProducto.Digital);
        pedido.pagar();

        servicio.entregar(pedido);

        assertEquals(EstadoPedido.Entregado, pedido.getEstado());
    }

    @Test
    void rechazaPedidosConProductosFisicos() {
        Pedido pedido = pedidoDe(TipoProducto.Fisico);
        pedido.pagar();

        assertThrows(IllegalStateException.class, () -> servicio.entregar(pedido));
        assertEquals(EstadoPedido.Pagado, pedido.getEstado());
    }

    @Test
    void rechazaPedidosSinPagar() {
        Pedido pedido = pedidoDe(TipoProducto.Digital);

        assertThrows(IllegalStateException.class, () -> servicio.entregar(pedido));
        assertEquals(EstadoPedido.PendienteDePago, pedido.getEstado());
    }

    @Test
    void noSeEntregaDosVeces() {
        Pedido pedido = pedidoDe(TipoProducto.Digital);
        pedido.pagar();
        servicio.entregar(pedido);

        assertThrows(IllegalStateException.class, () -> servicio.entregar(pedido));
    }
}