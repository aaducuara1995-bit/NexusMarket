package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Envio;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoEnvio;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.valueobjects.Direccion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreacionEnvioServiceTest {

    private final CreacionEnvioService servicio = new CreacionEnvioService();

    private final Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);
    private final Comprador comprador = Fixtures.comprador(usuario);

    private Pedido pedido(TipoProducto tipo, boolean pagado) {
        Producto producto = Fixtures.producto(tipo);
        producto.publicar();
        Carrito carrito = new Carrito(comprador);
        carrito.agregarProducto(producto, 1);
        Pedido pedido = new Pedido(carrito);
        if (pagado)
            pedido.pagar();
        return pedido;
    }

    @Test
    void creaElEnvioDeUnPedidoFisicoPagado() {
        Pedido pedido = pedido(TipoProducto.Fisico, true);

        Envio envio = servicio.crear(pedido, comprador.getDireccionPrincipal(), List.of());

        assertEquals(EstadoEnvio.Pendiente, envio.getEstado());
        assertEquals(pedido.getId(), envio.getPedido().getId());
    }

    @Test
    void aceptaUnaDireccionAdicionalDelComprador() {
        Direccion otra = new Direccion("Carrera 5 # 6-7", "Bello", "Antioquia", "051050");
        comprador.agregarDireccionAdicional(otra);
        Pedido pedido = pedido(TipoProducto.Fisico, true);

        Envio envio = servicio.crear(pedido, otra, List.of());

        assertEquals(otra, envio.getDireccionEntrega());
    }

    @Test
    void rechazaUnaDireccionQueNoEsDelComprador() {
        Direccion ajena = new Direccion("Calle 99 # 1-1", "Cali", "Valle", "760001");
        Pedido pedido = pedido(TipoProducto.Fisico, true);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(pedido, ajena, List.of()));
    }

    @Test
    void losPedidosSoloDigitalesNoLlevanEnvio() {
        Pedido pedido = pedido(TipoProducto.Digital, true);

        assertThrows(IllegalStateException.class,
                () -> servicio.crear(pedido, comprador.getDireccionPrincipal(), List.of()));
    }

    @Test
    void rechazaElEnvioDeUnPedidoSinPagar() {
        Pedido pedido = pedido(TipoProducto.Fisico, false);

        assertThrows(IllegalStateException.class,
                () -> servicio.crear(pedido, comprador.getDireccionPrincipal(), List.of()));
    }

    @Test
    void rechazaCrearDosEnviosParaElMismoPedido() {
        Pedido pedido = pedido(TipoProducto.Fisico, true);
        Envio existente = servicio.crear(pedido, comprador.getDireccionPrincipal(), List.of());

        assertThrows(IllegalStateException.class,
                () -> servicio.crear(pedido, comprador.getDireccionPrincipal(), List.of(existente)));
    }
}