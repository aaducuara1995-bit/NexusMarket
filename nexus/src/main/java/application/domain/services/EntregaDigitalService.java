package application.domain.services;

import application.domain.entities.Pedido;
import application.domain.enums.EstadoPedido;

public class EntregaDigitalService {

    /**
     * Los productos digitales se entregan de inmediato tras el pago, sin pasar por logÃ­stica.
     * Un pedido con algÃºn producto fÃ­sico sigue el flujo normal de envÃ­o.
     */
    public void entregar(Pedido pedido) {
        if (pedido == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (!pedido.esSoloDigital())
            throw new IllegalStateException(
                    "El pedido contiene productos fÃ­sicos y debe pasar por logÃ­stica.");
        if (pedido.getEstado() != EstadoPedido.Pagado)
            throw new IllegalStateException("Solo se entrega un pedido digital ya pagado.");

        pedido.enviar();
        pedido.finalizar();
    }
}