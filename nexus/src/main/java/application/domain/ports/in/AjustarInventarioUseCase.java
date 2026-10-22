package application.domain.ports.in;

import application.domain.entities.Inventario;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;

/** Puerto de entrada: sube o baja las existencias de un inventario con un motivo. */
public interface AjustarInventarioUseCase {

    MovimientoInventario ajustar(Usuario actor, Vendedor vendedor, Inventario inventario,
                                 int diferencia, String motivo);
}