package application.domain.services;

import application.domain.ports.in.AgregarAlCarritoUseCase;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Inventario;
import application.domain.entities.ItemCarrito;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.exceptions.AccesoDenegadoException;

import java.util.List;

public class AgregarAlCarritoService implements AgregarAlCarritoUseCase {

    private final ValidacionCompradorService validacionComprador;
    private final ConsultaDisponibilidadService consulta = new ConsultaDisponibilidadService();

    public AgregarAlCarritoService(ValidacionCompradorService validacionComprador) {
        if (validacionComprador == null)
            throw new IllegalArgumentException("El servicio de validación es obligatorio.");
        this.validacionComprador = validacionComprador;
    }

    public void agregar(Usuario usuario, Comprador comprador, Carrito carrito,
                        Producto producto, int cantidad, List<Inventario> inventarios) {
        validacionComprador.exigirHabilitado(usuario, comprador);
        if (carrito == null)
            throw new IllegalArgumentException("El carrito es obligatorio.");
        if (producto == null)
            throw new IllegalArgumentException("El producto es obligatorio.");
        if (inventarios == null)
            throw new IllegalArgumentException("La lista de inventarios es obligatoria.");
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");

        if (!carrito.getComprador().getId().equals(comprador.getId()))
            throw new AccesoDenegadoException("El carrito pertenece a otro comprador.");
        if (!producto.estaPublicado())
            throw new IllegalStateException("Solo se pueden agregar productos publicados.");

        int yaEnElCarrito = carrito.getItems().stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .mapToInt(ItemCarrito::getCantidad)
                .sum();

        // Agregar al carrito no reserva stock, pero no se acepta pedir más de lo que existe.
        if (!consulta.hayDisponibilidad(producto, yaEnElCarrito + cantidad, inventarios))
            throw new IllegalStateException(
                    "No hay existencias suficientes de " + producto.getNombre() + ".");

        carrito.agregarProducto(producto, cantidad);
    }
}