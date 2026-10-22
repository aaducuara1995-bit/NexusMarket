package application.domain.ports.in;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;

import java.util.List;

/** Puerto de entrada: el vendedor publica uno de sus productos en el catálogo. */
public interface PublicarProductoUseCase {

    void publicar(Usuario actor, Vendedor vendedor, Producto producto, List<Inventario> inventarios);
}