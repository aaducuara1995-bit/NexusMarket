package application.domain.ports.in;

import application.domain.entities.Pedido;

/** Puerto de entrada: entrega de inmediato un pedido pagado formado solo por productos digitales. */
public interface EntregarPedidoDigitalUseCase {

    void entregar(Pedido pedido);
}