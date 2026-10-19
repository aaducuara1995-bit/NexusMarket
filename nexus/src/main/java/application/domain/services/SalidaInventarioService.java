package application.domain.services;

import application.domain.entities.MovimientoInventario;
import application.domain.entities.ReservaInventario;
import application.domain.enums.TipoMovimientoInventario;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SalidaInventarioService {

    public List<MovimientoInventario> confirmarSalida(UUID pedidoId,
                                                      List<ReservaInventario> reservas) {
        if (pedidoId == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (reservas == null)
            throw new IllegalArgumentException("La lista de reservas es obligatoria.");

        // Se valida todo antes de modificar algo, para no confirmar a medias.
        for (ReservaInventario reserva : reservas) {
            if (!reserva.perteneceAPedido(pedidoId))
                throw new IllegalArgumentException("Hay reservas que no pertenecen al pedido.");
            if (!reserva.estaActiva())
                throw new IllegalStateException("Solo se puede dar salida a reservas activas.");
        }

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (ReservaInventario reserva : reservas) {
            reserva.confirmarSalida();
            movimientos.add(new MovimientoInventario(
                    reserva.getInventario(), TipoMovimientoInventario.SalidaPorVenta,
                    -reserva.getCantidad(),
                    "Salida por venta del pedido " + pedidoId));
        }
        return movimientos;
    }
}