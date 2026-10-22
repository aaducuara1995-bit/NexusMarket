package application.domain.ports.in;

import application.domain.entities.Factura;
import application.domain.entities.Pedido;

import java.util.List;

/** Puerto de entrada: emite la factura de un pedido pagado. */
public interface EmitirFacturaUseCase {

    Factura emitir(Pedido pedido, List<Factura> existentes);
}