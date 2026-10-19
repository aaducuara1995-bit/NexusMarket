package application.domain.entities;

import application.domain.enums.EstadoDevolucion;
import application.domain.enums.EstadoPedido;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Devolucion {

    private final UUID id;
    private final Pedido pedido;
    private final String motivo;
    private EstadoDevolucion estado;
    private final Map<UUID, Integer> unidadesReintegradas = new HashMap<>();

    public Devolucion(Pedido pedido, String motivo) {
        if (pedido == null)
            throw new IllegalArgumentException("La devoluciÃ³n debe estar asociada a un pedido.");
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("El motivo de la devoluciÃ³n es obligatorio.");
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
            throw new IllegalStateException("La devoluciÃ³n ya fue resuelta.");
    }

    public boolean estaAprobada() {
        return estado == EstadoDevolucion.Aprobada;
    }

    /** Una devoluciÃ³n solicitada o aprobada impide abrir otra sobre el mismo pedido. */
    public boolean estaAbierta() {
        return estado != EstadoDevolucion.Rechazada;
    }

    public int unidadesCompradas(UUID productoId) {
        return pedido.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .mapToInt(ItemPedido::getCantidad)
                .sum();
    }

    public int unidadesReintegradas(UUID productoId) {
        return unidadesReintegradas.getOrDefault(productoId, 0);
    }

    public int unidadesPendientesDeReintegro(UUID productoId) {
        return unidadesCompradas(productoId) - unidadesReintegradas(productoId);
    }

    public void registrarReintegro(UUID productoId, int unidades) {
        if (estado != EstadoDevolucion.Aprobada)
            throw new IllegalStateException("Solo se registra el reintegro de una devoluciÃ³n aprobada.");
        if (unidades <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        if (unidadesCompradas(productoId) == 0)
            throw new IllegalArgumentException("El producto no pertenece al pedido devuelto.");
        if (unidades > unidadesPendientesDeReintegro(productoId))
            throw new IllegalArgumentException(
                    "No se pueden reintegrar mÃ¡s unidades de las compradas en el pedido.");

        unidadesReintegradas.merge(productoId, unidades, Integer::sum);
    }

    public UUID getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public String getMotivo() { return motivo; }
    public EstadoDevolucion getEstado() { return estado; }
}