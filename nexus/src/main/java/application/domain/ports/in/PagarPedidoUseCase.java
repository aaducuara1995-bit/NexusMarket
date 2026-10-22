package application.domain.ports.in;

import application.domain.entities.Comprador;
import application.domain.entities.Pedido;
import application.domain.entities.Usuario;
import application.domain.valueobjects.Dinero;

/** Puerto de entrada: el comprador paga el total de su pedido. */
public interface PagarPedidoUseCase {

    void pagar(Usuario usuario, Comprador comprador, Pedido pedido, Dinero montoPagado);
}