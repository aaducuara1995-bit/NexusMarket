package application.domain.entities;

import application.domain.enums.EstadoPedido;
import application.domain.valueobjects.Dinero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Pedido {

    private final UUID id;
    private final Comprador comprador;
    private final List<ItemPedido> items = new ArrayList<>();
    private final LocalDateTime fecha;
    private EstadoPedido estado;

    public Pedido(Carrito carrito) {
        if (carrito == null)
            throw new IllegalArgumentException("El carrito es obligatorio.");
        if (carrito.getItems().isEmpty())
            throw new IllegalArgumentException("No se puede crear un pedido con un carrito vacío.");

        this.id = UUID.randomUUID();
        this.comprador = carrito.getComprador();
        carrito.getItems().forEach(
                i -> items.add(new ItemPedido(i.getProducto(), i.getCantidad())));
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.PendienteDePago;
    }

    public Dinero calcularTotal() {
        BigDecimal total = items.stream()
                .map(i -> i.subtotal().monto())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Dinero(total);
    }

    public void pagar() {
        exigirEstado(EstadoPedido.PendienteDePago, "El pedido no está pendiente de pago.");
        estado = EstadoPedido.Pagado;
    }

    public void enviar() {
        exigirEstado(EstadoPedido.Pagado, "El pedido debe estar pagado.");
        estado = EstadoPedido.Despachado;
    }

    public void finalizar() {
        exigirEstado(EstadoPedido.Despachado, "El pedido debe estar despachado.");
        estado = EstadoPedido.Entregado;
    }

    private void exigirEstado(EstadoPedido esperado, String mensaje) {
        if (estado != esperado)
            throw new IllegalStateException(mensaje);
    }

    public UUID getId() { return id; }
    public Comprador getComprador() { return comprador; }
    public LocalDateTime getFecha() { return fecha; }
    public EstadoPedido getEstado() { return estado; }
    public List<ItemPedido> getItems() { return Collections.unmodifiableList(items); }
}