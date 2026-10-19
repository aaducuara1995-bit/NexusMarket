package application.domain.services;

import application.domain.entities.Bodega;
import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoBodega;
import application.domain.enums.TipoMovimientoInventario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroInventarioInicialServiceTest {

    private final RegistroInventarioInicialService servicio = new RegistroInventarioInicialService(
            new AccesoInventarioService(new AutorizacionService(), new ValidacionVendedorService()));

    @Test
    void elVendedorRegistraExistenciasInicialesEnSuBodega() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Bodega bodega = Bodega.deVendedor(vendedor.getId(), "Mi bodega",
                TipoBodega.Fisica, Fixtures.direccion());

        var resultado = servicio.registrar(usuario, vendedor, producto, bodega, 10, List.of());

        assertEquals(10, resultado.inventario().getCantidad());
        assertEquals(1, resultado.movimientos().size());
        assertEquals(TipoMovimientoInventario.Ingreso, resultado.movimientos().get(0).getTipo());
        assertEquals(10, resultado.movimientos().get(0).getCantidad());
    }

    @Test
    void elVendedorTambienPuedeUsarUnaBodegaDelMarketplace() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);

        var resultado = servicio.registrar(usuario, vendedor, producto, Fixtures.bodega(), 5, List.of());

        assertEquals(5, resultado.inventario().getCantidad());
    }

    @Test
    void elVendedorNoPuedeUsarLaBodegaDeOtroVendedor() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Bodega ajena = Bodega.deVendedor(UUID.randomUUID(), "Bodega ajena",
                TipoBodega.Fisica, Fixtures.direccion());

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.registrar(usuario, vendedor, producto, ajena, 10, List.of()));
    }

    @Test
    void noSePuedeDuplicarElProductoEnLaMismaBodega() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Bodega bodega = Fixtures.bodega();
        Inventario existente = new Inventario(producto, bodega, 3);

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(usuario, vendedor, producto, bodega, 10, List.of(existente)));
    }

    @Test
    void soloLosProductosFisicosLlevanInventario() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto digital = Fixtures.producto(vendedor.getId(), TipoProducto.Digital);

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(usuario, vendedor, digital, Fixtures.bodega(), 10, List.of()));
    }

    @Test
    void elOperadorLogisticoPuedeRegistrarEnCualquierBodega() {
        Usuario operador = Fixtures.usuario(RolUsuario.OperadorLogistico);
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Bodega deVendedor = Bodega.deVendedor(UUID.randomUUID(), "Bodega de un vendedor",
                TipoBodega.Fisica, Fixtures.direccion());

        var resultado = servicio.registrar(operador, null, producto, deVendedor, 8, List.of());

        assertEquals(8, resultado.inventario().getCantidad());
    }

    @Test
    void unaExistenciaInicialDeCeroNoGeneraMovimiento() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor vendedor = Fixtures.vendedor(usuario);
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);

        var resultado = servicio.registrar(usuario, vendedor, producto, Fixtures.bodega(), 0, List.of());

        assertTrue(resultado.movimientos().isEmpty());
    }
}