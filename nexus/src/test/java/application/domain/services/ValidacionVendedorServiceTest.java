package application.domain.services;

import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidacionVendedorServiceTest {

    private final ValidacionVendedorService servicio = new ValidacionVendedorService();

    @Test
    void unVendedorActivoPuedeOperarSuProducto() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);

        assertDoesNotThrow(() -> servicio.exigirPropietario(usuario, vendedor, producto));
    }

    @Test
    void unUsuarioConOtroRolEsRechazado() {
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);
        Vendedor vendedor = Fixtures.vendedor(comprador);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.exigirVendedor(comprador, vendedor));
    }

    @Test
    void elVendedorDebeCorresponderAlUsuario() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor deOtraPersona = Fixtures.vendedor(Fixtures.usuario(RolUsuario.Vendedor));

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.exigirVendedor(usuario, deOtraPersona));
    }

    @Test
    void unProductoDeOtroVendedorEsRechazado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto ajeno = Fixtures.producto(TipoProducto.Fisico);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.exigirPropietario(usuario, vendedor, ajeno));
    }

    @Test
    void unUsuarioBloqueadoEsRechazado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        usuario.bloquear();

        assertThrows(IllegalStateException.class,
                () -> servicio.exigirVendedor(usuario, vendedor));
    }
}