package application.domain.ports.in;

import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.TipoProducto;
import application.domain.valueobjects.Dinero;

/** Puerto de entrada: el vendedor registra un producto nuevo, que nace suspendido. */
public interface RegistrarProductoUseCase {

    Producto registrar(Usuario actor, Vendedor vendedor, String nombre, String descripcion,
                       TipoProducto tipo, Dinero precio);
}