package application.domain.entities;

import application.domain.enums.TipoMovimientoInventario;

import java.time.LocalDateTime;
import java.util.UUID;

public class MovimientoInventario {

    private final UUID id;
    private final UUID inventarioId;
    private final UUID productoId;
    private final UUID bodegaId;
    private final TipoMovimientoInventario tipo;
    private final int cantidad;
    private final String motivo;
    private final LocalDateTime fecha;

    public MovimientoInventario(Inventario inventario,
                                TipoMovimientoInventario tipo,
                                int cantidad,
                                String motivo) {
        if (inventario == null)
            throw new IllegalArgumentException("El inventario es obligatorio.");
        if (tipo == null)
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio.");
        if (cantidad == 0)
            throw new IllegalArgumentException("La cantidad del movimiento no puede ser cero.");
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("El motivo del movimiento es obligatorio.");

        this.id = UUID.randomUUID();
        this.inventarioId = inventario.getId();
        this.productoId = inventario.getProducto().getId();
        this.bodegaId = inventario.getBodega().getId();
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.motivo = motivo.trim();
        this.fecha = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getInventarioId() { return inventarioId; }
    public UUID getProductoId() { return productoId; }
    public UUID getBodegaId() { return bodegaId; }
    public TipoMovimientoInventario getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFecha() { return fecha; }
}

