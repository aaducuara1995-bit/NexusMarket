package application.domain.ports.in;

import application.domain.entities.Devolucion;
import application.domain.entities.Inventario;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;

/** Puerto de entrada: devuelve al inventario las unidades de una devolución aprobada. */
public interface ReintegrarInventarioUseCase {

    MovimientoInventario reintegrar(Usuario actor, Vendedor vendedor, Inventario inventario,
                                    int unidades, Devolucion devolucion);
}