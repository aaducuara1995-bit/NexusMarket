package application.domain.services;

import application.domain.entities.Envio;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Pedido;
import application.domain.entities.ReservaInventario;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoEnvio;
import application.domain.enums.EstadoPedido;
import application.domain.enums.ProcesoNegocio;

import java.util.List;

public class DespachoService {

    private final AutorizacionService autorizacion;
    private final SalidaInventarioService salidaInventario;

    public DespachoService(AutorizacionService autorizacion,
                           SalidaInventarioService salidaInventario) {
        if (autorizacion == null || salidaInventario == null)
            throw new IllegalArgumentException("Los servicios de apoyo son obligatorios.");
        this.autorizacion = autorizacion;
        this.salidaInventario = salidaInventario;
    }

    public List<MovimientoInventario> despachar(Usuario actor, Pedido pedido, Envio envio,
                                                List<ReservaInventario> reservas) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.OperacionDespachos);
        if (pedido == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (envio == null)
            throw new IllegalArgumentException("El envÃ­o es obligatorio.");
        if (reservas == null)
            throw new IllegalArgumentException("La lista de reservas es obligatoria.");

        // Se valida todo antes de modificar algo, para no despachar a medias.
        if (!envio.getPedido().getId().equals(pedido.getId()))
            throw new IllegalArgumentException("El envÃ­o no corresponde al pedido.");
        if (pedido.getEstado() != EstadoPedido.Pagado)
            throw new IllegalStateException("El pedido debe estar pagado para despacharse.");
        if (envio.getEstado() != EstadoEnvio.Pendiente)
            throw new IllegalStateException("El envÃ­o ya fue despachado.");
        if (pedido.contieneProductosFisicos() && reservas.isEmpty())
            throw new IllegalStateException("El pedido no tiene stock reservado para despachar.");

        // La salida de inventario valida las reservas antes de confirmar ninguna.
        List<MovimientoInventario> movimientos =
                salidaInventario.confirmarSalida(pedido.getId(), reservas);

        envio.despachar();
        pedido.enviar();
        return movimientos;
    }
}