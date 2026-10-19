package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.EstadoInventario;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarcadoInventarioServiceTest {

    private final MarcadoInventarioService servicio = new MarcadoInventarioService(
            new AccesoInventarioService(new AutorizacionService(), new ValidacionVendedorService()));

    @Test
    void elVendedorMarcaSuInventarioComoDanado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);

        servicio.marcarDanado(usuario, vendedor, inventario);

        assertEquals(EstadoInventario.Danado, inventario.getEstado());
    }

    @Test
    void noSeMarcaComoDanadoUnInventarioConReservasActivas() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        inventario.reservar(2);

        assertThrows(IllegalStateException.class,
                () -> servicio.marcarDanado(usuario, vendedor, inventario));
        assertEquals(EstadoInventario.Disponible, inventario.getEstado());
    }

    @Test
    void sePuedeBloquearYVolverAHabilitar() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);

        servicio.bloquear(usuario, vendedor, inventario);
        assertEquals(EstadoInventario.Bloqueado, inventario.getEstado());

        servicio.habilitar(usuario, vendedor, inventario);
        assertEquals(EstadoInventario.Disponible, inventario.getEstado());
    }

    @Test
    void unCompradorNoPuedeMarcarInventario() {
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);
        Inventario inventario = Fixtures.inventario(Fixtures.producto(TipoProducto.Fisico), 10);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.marcarDanado(comprador, null, inventario));
    }

    @Test
    void elOperadorLogisticoPuedeMarcarInventario() {
        Usuario operador = Fixtures.usuario(RolUsuario.OperadorLogistico);
        Inventario inventario = Fixtures.inventario(Fixtures.producto(TipoProducto.Fisico), 10);

        servicio.marcarDanado(operador, null, inventario);

        assertEquals(EstadoInventario.Danado, inventario.getEstado());
    }
}