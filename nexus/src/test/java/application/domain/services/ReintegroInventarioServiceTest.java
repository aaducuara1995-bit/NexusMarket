package application.domain.services;

import application.domain.entities.Devolucion;
import application.domain.entities.Inventario;
import application.domain.entities.Pedido;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoMovimientoInventario;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReintegroInventarioServiceTest {

    private final ReintegroInventarioService servicio = new ReintegroInventarioService(
            new AccesoInventarioService(new AutorizacionService(), new ValidacionVendedorService()));

    private Devolucion devolucionAprobada(Producto producto, int cantidadComprada) {
        Pedido pedido = Fixtures.pedidoEntregado(producto, cantidadComprada);
        Devolucion devolucion = new Devolucion(pedido, "Talla incorrecta");
        devolucion.aprobar();
        return devolucion;
    }

    @Test
    void reintegraLasUnidadesDeUnaDevolucionAprobada() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Devolucion devolucion = devolucionAprobada(producto, 3);

        var movimiento = servicio.reintegrar(usuario, vendedor, inventario, 2, devolucion);

        assertEquals(12, inventario.getCantidad());
        assertEquals(TipoMovimientoInventario.Devolucion, movimiento.getTipo());
        assertEquals(2, movimiento.getCantidad());
    }

    @Test
    void rechazaUnaDevolucionQueNoEstaAprobada() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Pedido pedido = Fixtures.pedidoEntregado(producto, 3);
        Devolucion solicitada = new Devolucion(pedido, "Talla incorrecta");

        assertThrows(IllegalStateException.class,
                () -> servicio.reintegrar(usuario, vendedor, inventario, 2, solicitada));
        assertEquals(10, inventario.getCantidad());
    }

    @Test
    void rechazaUnProductoQueNoEstabaEnElPedido() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto comprado = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Producto otro = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventarioDelOtro = Fixtures.inventario(otro, 10);
        Devolucion devolucion = devolucionAprobada(comprado, 3);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.reintegrar(usuario, vendedor, inventarioDelOtro, 1, devolucion));
    }

    @Test
    void rechazaReintegrarMasUnidadesDeLasCompradas() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Devolucion devolucion = devolucionAprobada(producto, 3);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.reintegrar(usuario, vendedor, inventario, 4, devolucion));
        assertEquals(10, inventario.getCantidad());
    }

    @Test
    void soloReintegraAUnInventarioDisponible() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        inventario.bloquear();
        Devolucion devolucion = devolucionAprobada(producto, 3);

        assertThrows(IllegalStateException.class,
                () -> servicio.reintegrar(usuario, vendedor, inventario, 2, devolucion));
        assertEquals(10, inventario.getCantidad());
    }
}