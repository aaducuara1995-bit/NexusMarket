package application.domain.entities;

import application.domain.enums.EstadoDevolucion;
import application.domain.enums.EstadoPedido;

import java.util.UUID;

public class Devolucion {

    private final UUID id;
    private final Pedido pedido;
    private final String motivo;
    private EstadoDevolucion estado;

    public Devolucion(Pedido pedido, String motivo) {
        if (pedido == null)
            throw new IllegalArgumentException("La devolución debe estar asociada a un pedido.");
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("El motivo de la devolución es obligatorio.");
        if (pedido.getEstado() != EstadoPedido.Entregado)
            throw new IllegalStateException("Solo se puede devolver un pedido ya entregado.");

        this.id = UUID.randomUUID();
        this.pedido = pedido;
        this.motivo = motivo.trim();
        this.estado = EstadoDevolucion.Solicitada;
    }

    public void aprobar() {
        exigirSolicitada();
        estado = EstadoDevolucion.Aprobada;
    }

    public void rechazar() {
        exigirSolicitada();
        estado = EstadoDevolucion.Rechazada;
    }

    private void exigirSolicitada() {
        if (estado != EstadoDevolucion.Solicitada)
            throw new IllegalStateException("La devolución ya fue resuelta.");
    }

    public UUID getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public String getMotivo() { return motivo; }
    public EstadoDevolucion getEstado() { return estado; }
}