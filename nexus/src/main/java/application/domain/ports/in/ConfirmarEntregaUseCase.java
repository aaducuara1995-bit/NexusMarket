package application.domain.ports.in;

import application.domain.entities.Envio;
import application.domain.entities.Pedido;
import application.domain.entities.Usuario;

/** Puerto de entrada: el operador logístico confirma la entrega y el pedido queda finalizado. */
public interface ConfirmarEntregaUseCase {

    void confirmar(Usuario actor, Pedido pedido, Envio envio);
}