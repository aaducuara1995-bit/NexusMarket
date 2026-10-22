package application.domain.ports.in;

import application.domain.entities.Envio;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Pedido;
import application.domain.entities.ReservaInventario;
import application.domain.entities.Usuario;

import java.util.List;

/** Puerto de entrada: el operador logístico despacha un pedido y se descuenta el stock reservado. */
public interface DespacharPedidoUseCase {

    List<MovimientoInventario> despachar(Usuario actor, Pedido pedido, Envio envio,
                                         List<ReservaInventario> reservas);
}