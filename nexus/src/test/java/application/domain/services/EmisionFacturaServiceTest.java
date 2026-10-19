package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Factura;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmisionFacturaServiceTest {

    private final EmisionFacturaService servicio = new EmisionFacturaService();

    private Pedido pedido(boolean pagado) {
        Producto producto = Fixtures.producto(TipoProducto.Digital);
        producto.publicar();
        Carrito carrito = new Carrito(Fixtures.comprador(Fixtures.usuario(RolUsuario.Comprador)));
        carrito.agregarProducto(producto, 2);
        Pedido pedido = new Pedido(carrito);
        if (pagado)
            pedido.pagar();
        return pedido;
    }

    @Test
    void emiteLaFacturaDeUnPedidoPagadoConElTotalDelPedido() {
        Pedido pedido = pedido(true);

        Factura factura = servicio.emitir(pedido, List.of());

        assertEquals(pedido.getId(), factura.getPedido().getId());
        assertEquals(0, factura.getTotal().monto().compareTo(pedido.calcularTotal().monto()));
    }

    @Test
    void rechazaFacturarUnPedidoSinPagar() {
        Pedido pedido = pedido(false);

        assertThrows(IllegalStateException.class, () -> servicio.emitir(pedido, List.of()));
    }

    @Test
    void rechazaFacturarDosVecesElMismoPedido() {
        Pedido pedido = pedido(true);
        Factura existente = servicio.emitir(pedido, List.of());

        assertThrows(IllegalStateException.class,
                () -> servicio.emitir(pedido, List.of(existente)));
    }

    @Test
    void laFacturaDeOtroPedidoNoImpideFacturar() {
        Factura deOtroPedido = servicio.emitir(pedido(true), List.of());
        Pedido pedido = pedido(true);

        Factura factura = servicio.emitir(pedido, List.of(deOtroPedido));

        assertEquals(pedido.getId(), factura.getPedido().getId());
    }
}