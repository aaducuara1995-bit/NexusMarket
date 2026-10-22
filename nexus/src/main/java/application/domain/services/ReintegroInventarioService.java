package application.domain.services;

import application.domain.ports.in.ReintegrarInventarioUseCase;

import application.domain.entities.Devolucion;
import application.domain.entities.Inventario;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.EstadoDevolucion;
import application.domain.enums.TipoMovimientoInventario;

import java.util.UUID;

public class ReintegroInventarioService implements ReintegrarInventarioUseCase {

    private final AccesoInventarioService acceso;

    public ReintegroInventarioService(AccesoInventarioService acceso) {
        if (acceso == null)
            throw new IllegalArgumentException("El servicio de acceso es obligatorio.");
        this.acceso = acceso;
    }

    public MovimientoInventario reintegrar(Usuario actor, Vendedor vendedor, Inventario inventario,
                                           int unidades, Devolucion devolucion) {
        if (inventario == null)
            throw new IllegalArgumentException("El inventario es obligatorio.");
        if (devolucion == null)
            throw new IllegalArgumentException("La devolución es obligatoria.");
        acceso.exigirAcceso(actor, vendedor, inventario.getProducto());

        if (devolucion.getEstado() != EstadoDevolucion.Aprobada)
            throw new IllegalStateException("Solo se reintegra inventario de una devolución aprobada.");
        if (unidades <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");

        UUID productoId = inventario.getProducto().getId();
        if (devolucion.unidadesCompradas(productoId) == 0)
            throw new IllegalArgumentException("El producto no pertenece al pedido devuelto.");

        // La devolución recuerda lo ya reintegrado: no se devuelven unidades dos veces.
        if (unidades > devolucion.unidadesPendientesDeReintegro(productoId))
            throw new IllegalArgumentException(
                    "No se pueden reintegrar más unidades de las compradas en el pedido.");

        inventario.reintegrar(unidades);
        devolucion.registrarReintegro(productoId, unidades);

        return new MovimientoInventario(
                inventario, TipoMovimientoInventario.Devolucion, unidades,
                "Devolución del pedido " + devolucion.getPedido().getId());
    }
}