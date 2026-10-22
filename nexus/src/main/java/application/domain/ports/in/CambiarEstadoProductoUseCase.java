package application.domain.ports.in;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;

import java.util.List;

/** Puerto de entrada: el vendedor suspende o descontinúa uno de sus productos. */
public interface CambiarEstadoProductoUseCase {

    void suspender(Usuario actor, Vendedor vendedor, Producto producto);

    void descontinuar(Usuario actor, Vendedor vendedor, Producto producto, List<Inventario> inventarios);
}