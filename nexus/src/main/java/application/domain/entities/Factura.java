package application.domain.entities;

import application.domain.enums.EstadoPedido;
import application.domain.valueobjects.Dinero;

import java.time.LocalDateTime;
import java.util.UUID;

public class Factura {

    private final UUID id;
    private final Pedido pedido;
    private final Dinero total;
    private final LocalDateTime fecha;

    public Factura(Pedido pedido) {
        if (pedido == null)
            throw new IllegalArgumentException("La factura debe estar asociada a un pedido.");
        if (pedido.getEstado() == EstadoPedido.Carrito
                || pedido.getEstado() == EstadoPedido.PendienteDePago)
            throw new IllegalStateException("Solo se puede facturar un pedido que ya fue pagado.");

        this.id = UUID.randomUUID();
        this.pedido = pedido;
        this.total = pedido.calcularTotal();
        this.fecha = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public Dinero getTotal() { return total; }
    public LocalDateTime getFecha() { return fecha; }
}