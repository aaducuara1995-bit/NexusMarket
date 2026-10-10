package application.domain.entities;

import application.domain.enums.EstadoPedido;
import application.domain.enums.EstadoReembolso;
import application.domain.valueobjects.Dinero;

import java.util.UUID;

public class Reembolso {

    private final UUID id;
    private final Pedido pedido;
    private final Dinero monto;
    private EstadoReembolso estado;

    public Reembolso(Pedido pedido, Dinero monto) {
        if (pedido == null)
            throw new IllegalArgumentException("El reembolso debe estar asociado a un pedido.");
        if (monto == null || monto.monto().signum() <= 0)
            throw new IllegalArgumentException("El monto del reembolso debe ser mayor que cero.");
        if (pedido.getEstado() == EstadoPedido.Carrito
                || pedido.getEstado() == EstadoPedido.PendienteDePago)
            throw new IllegalStateException("No se puede reembolsar un pedido que no fue pagado.");

        Dinero totalPedido = pedido.calcularTotal();
        if (!monto.moneda().equals(totalPedido.moneda()))
            throw new IllegalArgumentException("El reembolso debe estar en la misma moneda del pedido.");
        if (monto.monto().compareTo(totalPedido.monto()) > 0)
            throw new IllegalArgumentException("El reembolso no puede superar el valor del pedido.");

        this.id = UUID.randomUUID();
        this.pedido = pedido;
        this.monto = monto;
        this.estado = EstadoReembolso.Pendiente;
    }

    public void procesar() {
        if (estado != EstadoReembolso.Pendiente)
            throw new IllegalStateException("El reembolso ya fue procesado.");
        estado = EstadoReembolso.Procesado;
    }

    public UUID getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public Dinero getMonto() { return monto; }
    public EstadoReembolso getEstado() { return estado; }
}