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

class PublicacionProductoServiceTest {

    private final PublicacionProductoService servicio = new PublicacionProductoService(
            new AutorizacionService(), new ValidacionVendedorService());

    @Test
    void publicaUnProductoFisicoConExistencias() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 5);

        servicio.publicar(usuario, vendedor, producto, List.of(inventario));

        assertEquals(EstadoProducto.Publicado, producto.getEstado());
    }

    @Test
    void rechazaUnProductoFisicoSinExistencias() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);

        assertThrows(IllegalStateException.class,
                () -> servicio.publicar(usuario, vendedor, producto, List.of()));
        assertEquals(EstadoProducto.Suspendido, producto.getEstado());
    }

    @Test
    void noCuentaElInventarioDanado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario danado = Fixtures.inventario(producto, 5);
        danado.marcarDanado();

        assertThrows(IllegalStateException.class,
                () -> servicio.publicar(usuario, vendedor, producto, List.of(danado)));
    }

    @Test
    void publicaUnProductoDigitalSinInventario() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Digital);

        servicio.publicar(usuario, vendedor, producto, List.of());

        assertEquals(EstadoProducto.Publicado, producto.getEstado());
    }

    @Test
    void unVendedorNoPuedePublicarElProductoDeOtro() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto ajeno = Fixtures.producto(TipoProducto.Digital);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.publicar(usuario, vendedor, ajeno, List.of()));
    }

    @Test
    void unCompradorNoPuedePublicarProductos() {
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);
        Vendedor vendedor = Fixtures.vendedor(comprador);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Digital);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.publicar(comprador, vendedor, producto, List.of()));
    }

    @Test
    void unProductoDescontinuadoNoSePublicaDeNuevo() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Digital);
        producto.descontinuar();

        assertThrows(IllegalStateException.class,
                () -> servicio.publicar(usuario, vendedor, producto, List.of()));
    }
}