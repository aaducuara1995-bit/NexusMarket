package application.domain.services;

import application.domain.entities.MovimientoInventario;
import application.domain.entities.ReservaInventario;
import application.domain.enums.TipoMovimientoInventario;

import java.util.ArrayList;
import java.util.List;

public class LiberacionReservaService {

    public List<MovimientoInventario> liberar(List<ReservaInventario> reservas) {
        if (reservas == null)
            throw new IllegalArgumentException("La lista de reservas es obligatoria.");

        // Se valida todo antes de modificar algo, para no liberar a medias.
        for (ReservaInventario reserva : reservas) {
            if (!reserva.estaActiva())
                throw new IllegalStateException("Solo se pueden liberar reservas activas.");
        }

        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (ReservaInventario reserva : reservas) {
            reserva.liberar();
            movimientos.add(new MovimientoInventario(
                    reserva.getInventario(), TipoMovimientoInventario.Reserva,
                    -reserva.getCantidad(),
                    "LiberaciÃ³n de la reserva del pedido " + reserva.getPedidoId()));
        }
        return movimientos;
    }
}