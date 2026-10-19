package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoMovimientoInventario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AjusteInventarioServiceTest {

    private final AjusteInventarioService servicio = new AjusteInventarioService(
            new AccesoInventarioService(new AutorizacionService(), new ValidacionVendedorService()));

    @Test
    void unAjustePositivoSumaYRegistraElMovimiento() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);

        var movimiento = servicio.ajustar(usuario, vendedor, inventario, 5, "Conteo fisico");

        assertEquals(15, inventario.getCantidad());
        assertEquals(TipoMovimientoInventario.Ajuste, movimiento.getTipo());
        assertEquals(5, movimiento.getCantidad());
    }

    @Test
    void unAjusteNegativoRestaYRegistraElMovimiento() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);

        var movimiento = servicio.ajustar(usuario, vendedor, inventario, -3, "Merma");

        assertEquals(7, inventario.getCantidad());
        assertEquals(-3, movimiento.getCantidad());
    }

    @Test
    void noPermiteDejarExistenciasNegativas() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);

        assertThrows(IllegalStateException.class,
                () -> servicio.ajustar(usuario, vendedor, inventario, -11, "Merma"));
        assertEquals(10, inventario.getCantidad());
    }

    @Test
    void exigeUnMotivoYNoModificaNadaSiFalta() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.ajustar(usuario, vendedor, inventario, 5, "  "));
        assertEquals(10, inventario.getCantidad());
    }

    @Test
    void unVendedorNoAjustaElInventarioDeOtro() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Inventario ajeno = Fixtures.inventario(Fixtures.producto(TipoProducto.Fisico), 10);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.ajustar(usuario, vendedor, ajeno, 5, "Conteo fisico"));
        assertEquals(10, ajeno.getCantidad());
    }
}