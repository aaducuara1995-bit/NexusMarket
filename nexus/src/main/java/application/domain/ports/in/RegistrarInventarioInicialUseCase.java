package application.domain.ports.in;

import application.domain.entities.Bodega;
import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.services.RegistroInventarioInicialService;

import java.util.List;

/** Puerto de entrada: registra las existencias iniciales de un producto en una bodega. */
public interface RegistrarInventarioInicialUseCase {

    RegistroInventarioInicialService.Resultado registrar(Usuario actor, Vendedor vendedor, Producto producto,
                                                         Bodega bodega, int cantidadInicial,
                                                         List<Inventario> existentes);
}