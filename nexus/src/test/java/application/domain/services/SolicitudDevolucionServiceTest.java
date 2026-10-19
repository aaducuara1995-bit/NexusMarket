package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Devolucion;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoDevolucion;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolicitudDevolucionServiceTest {

    private final SolicitudDevolucionService servicio =
            new SolicitudDevolucionService(new AutorizacionService());

    private final Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);
    private final Comprador comprador = Fixtures.comprador(usuario);

    private Pedido pedidoDe(Comprador duenio, boolean entregado) {
        Producto producto = Fixtures.producto(TipoProducto.Digital);
        producto.publicar();
        Carrito carrito = new Carrito(duenio);
        carrito.agregarProducto(producto, 1);
        Pedido pedido = new Pedido(carrito);
        pedido.pagar();
        if (entregado) {
            pedido.enviar();
            pedido.finalizar();
        }
        return pedido;
    }

    @Test
    void elCompradorSolicitaLaDevolucionDeUnPedidoEntregado() {
        Pedido pedido = pedidoDe(comprador, true);

        Devolucion devolucion = servicio.solicitar(usuario, comprador, pedido, "Llego defectuoso", List.of());

        assertEquals(EstadoDevolucion.Solicitada, devolucion.getEstado());
        assertEquals(pedido.getId(), devolucion.getPedido().getId());
    }

    @Test
    void rechazaUnPedidoQueTodaviaNoSeEntrega() {
        Pedido pedido = pedidoDe(comprador, false);

        assertThrows(IllegalStateException.class,
                () -> servicio.solicitar(usuario, comprador, pedido, "Ya no lo quiero", List.of()));
    }

    @Test
    void rechazaElPedidoDeOtroComprador() {
        Pedido ajeno = pedidoDe(Fixtures.comprador(Fixtures.usuario(RolUsuario.Comprador)), true);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.solicitar(usuario, comprador, ajeno, "Llego defectuoso", List.of()));
    }

    @Test
    void rechazaSiElPedidoYaTieneUnaDevolucionEnCurso() {
        Pedido pedido = pedidoDe(comprador, true);
        Devolucion previa = servicio.solicitar(usuario, comprador, pedido, "Llego defectuoso", List.of());

        assertThrows(IllegalStateException.class,
                () -> servicio.solicitar(usuario, comprador, pedido, "Otro motivo", List.of(previa)));
    }

    @Test
    void permiteSolicitarDeNuevoSiLaAnteriorFueRechazada() {
        Pedido pedido = pedidoDe(comprador, true);
        Devolucion previa = servicio.solicitar(usuario, comprador, pedido, "Llego defectuoso", List.of());
        previa.rechazar();

        Devolucion nueva = servicio.solicitar(usuario, comprador, pedido, "Sigue defectuoso", List.of(previa));

        assertEquals(EstadoDevolucion.Solicitada, nueva.getEstado());
    }

    @Test
    void unVendedorNoPuedeSolicitarDevoluciones() {
        Usuario vendedor = Fixtures.usuario(RolUsuario.Vendedor);
        Comprador perfil = Fixtures.comprador(vendedor);
        Pedido pedido = pedidoDe(perfil, true);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.solicitar(vendedor, perfil, pedido, "Llego defectuoso", List.of()));
    }

    @Test
    void unAdministradorNoSolicitaEnNombreDelComprador() {
        Usuario admin = Fixtures.usuario(RolUsuario.Administrador);
        Pedido pedido = pedidoDe(comprador, true);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.solicitar(admin, comprador, pedido, "Llego defectuoso", List.of()));
    }
}