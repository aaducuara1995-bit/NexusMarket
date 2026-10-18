package application.domain.entities;

import application.domain.enums.EstadoReserva;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReservaInventario {

    private final UUID id;
    private final UUID pedidoId;
    private final Inventario inventario;
    private final int cantidad;
    private final LocalDateTime fecha;
    private EstadoReserva estado;

    private ReservaInventario(UUID pedidoId, Inventario inventario, int cantidad) {
        this.id = UUID.randomUUID();
        this.pedidoId = pedidoId;
        this.inventario = inventario;
        this.cantidad = cantidad;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoReserva.Activa;
    }

    public static ReservaInventario crear(UUID pedidoId, Inventario inventario, int cantidad) {
        if (pedidoId == null)
            throw new IllegalArgumentException("La reserva debe estar asociada a un pedido.");
        if (inventario == null)
            throw new IllegalArgumentException("El inventario es obligatorio.");

        inventario.reservar(cantidad);
        return new ReservaInventario(pedidoId, inventario, cantidad);
    }

    public void liberar() {
        exigirActiva();
        inventario.liberarReserva(cantidad);
        estado = EstadoReserva.Liberada;
    }

    public void confirmarSalida() {
        exigirActiva();
        inventario.confirmarSalida(cantidad);
        estado = EstadoReserva.Confirmada;
    }

    private void exigirActiva() {
        if (estado != EstadoReserva.Activa)
            throw new IllegalStateException("La reserva ya fue liberada o confirmada.");
    }

    public boolean estaActiva() {
        return estado == EstadoReserva.Activa;
    }

    public boolean perteneceAPedido(UUID idPedido) {
        return pedidoId.equals(idPedido);
    }

    public UUID getId() { return id; }
    public UUID getPedidoId() { return pedidoId; }
    public Inventario getInventario() { return inventario; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public EstadoReserva getEstado() { return estado; }
}

