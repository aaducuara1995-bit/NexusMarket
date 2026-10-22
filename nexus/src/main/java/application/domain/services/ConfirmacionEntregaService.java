package application.domain.services;

import application.domain.ports.in.ConfirmarEntregaUseCase;

import application.domain.entities.Envio;
import application.domain.entities.Pedido;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoEnvio;
import application.domain.enums.EstadoPedido;
import application.domain.enums.ProcesoNegocio;

public class ConfirmacionEntregaService implements ConfirmarEntregaUseCase {

    private final AutorizacionService autorizacion;

    public ConfirmacionEntregaService(AutorizacionService autorizacion) {
        if (autorizacion == null)
            throw new IllegalArgumentException("El servicio de autorización es obligatorio.");
        this.autorizacion = autorizacion;
    }

    public void confirmar(Usuario actor, Pedido pedido, Envio envio) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.OperacionDespachos);
        if (pedido == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (envio == null)
            throw new IllegalArgumentException("El envío es obligatorio.");

        if (!envio.getPedido().getId().equals(pedido.getId()))
            throw new IllegalArgumentException("El envío no corresponde al pedido.");
        if (envio.getEstado() != EstadoEnvio.Despachado)
            throw new IllegalStateException("Solo se confirma la entrega de un envío despachado.");
        if (pedido.getEstado() != EstadoPedido.Despachado)
            throw new IllegalStateException("El pedido debe estar despachado.");

        envio.entregar();
        pedido.finalizar();
    }
}