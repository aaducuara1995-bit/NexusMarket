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

class AccesoInventarioServiceTest {

    private final AccesoInventarioService servicio = new AccesoInventarioService(
            new AutorizacionService(), new ValidacionVendedorService());

    @Test
    void elVendedorDuenoDelProductoTieneAcceso() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);

        assertDoesNotThrow(() -> servicio.exigirAcceso(usuario, vendedor, producto));
    }

    @Test
    void elOperadorLogisticoTieneAccesoSinSerDueno() {
        Usuario operador = Fixtures.usuario(RolUsuario.OperadorLogistico);
        Producto producto = Fixtures.producto(TipoProducto.Fisico);

        assertDoesNotThrow(() -> servicio.exigirAcceso(operador, null, producto));
    }

    @Test
    void unVendedorNoAccedeAlInventarioDeProductosAjenos() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto ajeno = Fixtures.producto(TipoProducto.Fisico);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.exigirAcceso(usuario, vendedor, ajeno));
    }

    @Test
    void unCompradorNoTieneAcceso() {
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);
        Producto producto = Fixtures.producto(TipoProducto.Fisico);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.exigirAcceso(comprador, null, producto));
    }

    @Test
    void unVendedorSinPerfilEsRechazado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Producto producto = Fixtures.producto(TipoProducto.Fisico);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.exigirAcceso(usuario, null, producto));
    }
}