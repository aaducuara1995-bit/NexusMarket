package application.domain.ports.in;

import application.domain.entities.Carrito;
import application.domain.entities.Comprador;
import application.domain.entities.Inventario;
import application.domain.entities.Usuario;
import application.domain.services.ConfirmacionPedidoService;

import java.util.List;

/** Puerto de entrada: el comprador convierte su carrito en un pedido y se reserva el stock. */
public interface ConfirmarPedidoUseCase {

    ConfirmacionPedidoService.Resultado confirmar(Usuario usuario, Comprador comprador, Carrito carrito,
                                                  List<Inventario> inventarios);
}