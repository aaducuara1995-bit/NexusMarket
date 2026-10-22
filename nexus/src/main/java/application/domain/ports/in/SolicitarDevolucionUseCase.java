package application.domain.ports.in;

import application.domain.entities.Comprador;
import application.domain.entities.Devolucion;
import application.domain.entities.Pedido;
import application.domain.entities.Usuario;

import java.util.List;

/** Puerto de entrada: el comprador solicita la devolución de un pedido entregado. */
public interface SolicitarDevolucionUseCase {

    Devolucion solicitar(Usuario usuario, Comprador comprador, Pedido pedido,
                        String motivo, List<Devolucion> existentes);
}