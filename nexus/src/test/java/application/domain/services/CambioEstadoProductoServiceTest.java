package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.EstadoProducto;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CambioEstadoProductoServiceTest {

    private final CambioEstadoProductoService servicio = new CambioEstadoProductoService(
            new AutorizacionService(), new ValidacionVendedorService());

    @Test
    void elVendedorSuspendeSuProductoPublicado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Digital);
        producto.publicar();

        servicio.suspender(usuario, vendedor, producto);

        assertEquals(EstadoProducto.Suspendido, producto.getEstado());
    }

    @Test
    void elVendedorDescontinuaSuProducto() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Digital);

        servicio.descontinuar(usuario, vendedor, producto, List.of());

        assertEquals(EstadoProducto.Descontinuado, producto.getEstado());
    }

    @Test
    void noSeDescontinuaUnProductoConReservasActivas() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        inventario.reservar(2);

        assertThrows(IllegalStateException.class,
                () -> servicio.descontinuar(usuario, vendedor, producto, List.of(inventario)));
        assertEquals(EstadoProducto.Suspendido, producto.getEstado());
    }

    @Test
    void unProductoDescontinuadoNoPuedeSuspenderse() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Digital);
        servicio.descontinuar(usuario, vendedor, producto, List.of());

        assertThrows(IllegalStateException.class,
                () -> servicio.suspender(usuario, vendedor, producto));
    }

    @Test
    void unVendedorNoPuedeCambiarElProductoDeOtro() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto ajeno = Fixtures.producto(TipoProducto.Digital);
        ajeno.publicar();

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.suspender(usuario, vendedor, ajeno));
    }

    @Test
    void unCompradorNoPuedeSuspenderProductos() {
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);
        Vendedor vendedor = Fixtures.vendedor(comprador);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Digital);
        producto.publicar();

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.suspender(comprador, vendedor, producto));
    }
}