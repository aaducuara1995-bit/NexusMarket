package application.domain.ports.in;

import application.domain.entities.Envio;
import application.domain.entities.Pedido;
import application.domain.valueobjects.Direccion;

import java.util.List;

/** Puerto de entrada: crea el envío de un pedido pagado con productos físicos. */
public interface CrearEnvioUseCase {

    Envio crear(Pedido pedido, Direccion direccionEntrega, List<Envio> existentes);
}