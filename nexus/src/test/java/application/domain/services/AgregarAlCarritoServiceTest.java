package application.domain.services;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgregarAlCarritoServiceTest {

    private final AgregarAlCarritoService servicio =
            new AgregarAlCarritoService(new ValidacionCompradorService());

    private final Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);
    private final Comprador comprador = Fixtures.comprador(usuario);

    private Producto productoPublicado(TipoProducto tipo) {
        Producto producto = Fixtures.producto(tipo);
        producto.publicar();
        return producto;
    }

    @Test
    void agregaUnProductoPublicadoConExistencias() {
        Producto producto = productoPublicado(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Carrito carrito = new Carrito(comprador);

        servicio.agregar(usuario, comprador, carrito, producto, 3, List.of(inventario));

        assertEquals(1, carrito.getItems().size());
        assertEquals(3, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void sumaLaCantidadSiElProductoYaEstaEnElCarrito() {
        Producto producto = productoPublicado(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Carrito carrito = new Carrito(comprador);

        servicio.agregar(usuario, comprador, carrito, producto, 3, List.of(inventario));
        servicio.agregar(usuario, comprador, carrito, producto, 4, List.of(inventario));

        assertEquals(1, carrito.getItems().size());
        assertEquals(7, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void rechazaSiLaCantidadTotalSuperaLasExistencias() {
        Producto producto = productoPublicado(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 5);
        Carrito carrito = new Carrito(comprador);
        servicio.agregar(usuario, comprador, carrito, producto, 3, List.of(inventario));

        assertThrows(IllegalStateException.class,
                () -> servicio.agregar(usuario, comprador, carrito, producto, 3, List.of(inventario)));
        assertEquals(3, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void rechazaUnProductoQueNoEstaPublicado() {
        Producto producto = Fixtures.producto(TipoProducto.Fisico);
        Inventario inventario = Fixtures.inventario(producto, 10);
        Carrito carrito = new Carrito(comprador);

        assertThrows(IllegalStateException.class,
                () -> servicio.agregar(usuario, comprador, carrito, producto, 1, List.of(inventario)));
        assertTrue(carrito.getItems().isEmpty());
    }

    @Test
    void unProductoDigitalNoRequiereInventario() {
        Producto digital = productoPublicado(TipoProducto.Digital);
        Carrito carrito = new Carrito(comprador);

        servicio.agregar(usuario, comprador, carrito, digital, 2, List.of());

        assertEquals(2, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void unCompradorRestringidoNoPuedeAgregar() {
        Producto digital = productoPublicado(TipoProducto.Digital);
        Carrito carrito = new Carrito(comprador);
        comprador.restringir();

        assertThrows(IllegalStateException.class,
                () -> servicio.agregar(usuario, comprador, carrito, digital, 1, List.of()));
    }

    @Test
    void noSePuedeUsarElCarritoDeOtroComprador() {
        Producto digital = productoPublicado(TipoProducto.Digital);
        Carrito deOtro = new Carrito(Fixtures.comprador(Fixtures.usuario(RolUsuario.Comprador)));

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.agregar(usuario, comprador, deOtro, digital, 1, List.of()));
    }
}