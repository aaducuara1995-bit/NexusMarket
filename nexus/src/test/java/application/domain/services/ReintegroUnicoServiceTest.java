package application.domain.services;

import application.domain.entities.Devolucion;
import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReintegroUnicoServiceTest {

    private final ReintegroInventarioService servicio = new ReintegroInventarioService(
            new AccesoInventarioService(new AutorizacionService(), new ValidacionVendedorService()));

    private final Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
    private final Vendedor vendedor = Fixtures.vendedor(usuario);

    private Devolucion devolucionAprobada(Producto producto, int cantidadComprada) {
        Devolucion devolucion = new Devolucion(
                Fixtures.pedidoEntregado(producto, cantidadComprada), "Talla incorrecta");
        devolucion.aprobar();
        return devolucion;
    }

    @Test
    void sePuedeReintegrarEnVariasPartesHastaElTotalComprado() {
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario bodegaA = Fixtures.inventario(producto, 10);
        Inventario bodegaB = Fixtures.inventario(producto, 10);
        Devolucion devolucion = devolucionAprobada(producto, 3);

        servicio.reintegrar(usuario, vendedor, bodegaA, 2, devolucion);
        servicio.reintegrar(usuario, vendedor, bodegaB, 1, devolucion);

        assertEquals(12, bodegaA.getCantidad());
        assertEquals(11, bodegaB.getCantidad());
        assertEquals(0, devolucion.unidadesPendientesDeReintegro(producto.getId()));
    }

    @Test
    void noSeReintegraDosVecesLaMismaDevolucion() {
        Producto producto = Fixtures.producto(vendedor.getId(), TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Devolucion devolucion = devolucionAprobada(producto, 3);
        servicio.reintegrar(usuario, vendedor, inventario, 3, devolucion);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.reintegrar(usuario, vendedor, inventario, 1, devolucion));
        assertEquals(13, inventario.getCantidad());
    }
}