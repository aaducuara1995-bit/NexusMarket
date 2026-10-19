package application.domain.services;

import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.EstadoProducto;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import application.domain.valueobjects.Dinero;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistroProductoServiceTest {

    private final RepositoriosEnMemoria.Productos repo = new RepositoriosEnMemoria.Productos();
    private final RegistroProductoService servicio = new RegistroProductoService(
            new AutorizacionService(), new ValidacionVendedorService(), repo);

    private final Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
    private final Vendedor vendedor = Fixtures.vendedor(usuario);
    private final Dinero precio = new Dinero(new BigDecimal("45000"));

    @Test
    void elVendedorRegistraUnProductoQueNaceSuspendido() {
        Producto producto = servicio.registrar(usuario, vendedor, "Camiseta", "Algodon",
                TipoProducto.Fisico, precio);

        assertEquals(EstadoProducto.Suspendido, producto.getEstado());
        assertEquals(vendedor.getId(), producto.getVendedorId());
        assertEquals(1, repo.guardados.size());
    }

    @Test
    void unVendedorNoRepiteElNombreDeSusProductos() {
        servicio.registrar(usuario, vendedor, "Camiseta", "Algodon", TipoProducto.Fisico, precio);

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(usuario, vendedor, "camiseta", "Otra", TipoProducto.Fisico, precio));
        assertEquals(1, repo.guardados.size());
    }

    @Test
    void dosVendedoresPuedenUsarElMismoNombre() {
        Usuario otroUsuario = Fixtures.usuario(RolUsuario.Vendedor);
        Vendedor otro = Fixtures.vendedor(otroUsuario);
        servicio.registrar(usuario, vendedor, "Camiseta", "Algodon", TipoProducto.Fisico, precio);

        servicio.registrar(otroUsuario, otro, "Camiseta", "Lino", TipoProducto.Fisico, precio);

        assertEquals(2, repo.guardados.size());
    }

    @Test
    void unCompradorNoRegistraProductos() {
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.registrar(comprador, Fixtures.vendedor(comprador), "Camiseta",
                        "Algodon", TipoProducto.Fisico, precio));
    }

    @Test
    void rechazaUnPrecioDeCeroSinGuardarNada() {
        Dinero cero = new Dinero(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrar(usuario, vendedor, "Camiseta", "Algodon", TipoProducto.Fisico, cero));
        assertEquals(0, repo.guardados.size());
    }
}