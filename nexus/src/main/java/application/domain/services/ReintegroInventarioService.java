package application.domain.services;

import application.domain.entities.Devolucion;
import application.domain.entities.Inventario;
import application.domain.entities.ItemPedido;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.EstadoDevolucion;
import application.domain.enums.TipoMovimientoInventario;

import java.util.UUID;

public class ReintegroInventarioService {

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
            throw new IllegalArgumentException("La devoluciÃ³n es obligatoria.");
        acceso.exigirAcceso(actor, vendedor, inventario.getProducto());

        if (devolucion.getEstado() != EstadoDevolucion.Aprobada)
            throw new IllegalStateException("Solo se reintegra inventario de una devoluciÃ³n aprobada.");
        if (unidades <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");

        UUID productoId = inventario.getProducto().getId();
        int compradas = devolucion.getPedido().getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .mapToInt(ItemPedido::getCantidad)
                .sum();

        if (compradas == 0)
            throw new IllegalArgumentException("El producto no pertenece al pedido devuelto.");
        if (unidades > compradas)
            throw new IllegalArgumentException(
                    "No se pueden reintegrar mÃ¡s unidades de las que se compraron.");

        inventario.reintegrar(unidades);

        return new MovimientoInventario(
                inventario, TipoMovimientoInventario.Devolucion, unidades,
                "DevoluciÃ³n del pedido " + devolucion.getPedido().getId());
    }
}