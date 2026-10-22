package application.domain.ports.in;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;

import java.util.List;

/** Puerto de entrada: el comprador agrega un producto publicado a su carrito. */
public interface AgregarAlCarritoUseCase {

    void agregar(Usuario usuario, Comprador comprador, Carrito carrito,
                 Producto producto, int cantidad, List<Inventario> inventarios);
}