package application.domain.entities;

import application.domain.enums.EstadoProducto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Carrito {

    private final UUID id;
    private final Comprador comprador;
    private final List<ItemCarrito> items = new ArrayList<>();

    public Carrito(Comprador comprador) {
        if (comprador == null)
            throw new IllegalArgumentException("El carrito debe pertenecer a un comprador.");

        this.id = UUID.randomUUID();
        this.comprador = comprador;
    }

    public void agregarProducto(Producto producto, int cantidad) {
        if (producto == null)
            throw new IllegalArgumentException("El producto es obligatorio.");
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        if (producto.getEstado() != EstadoProducto.Publicado)
            throw new IllegalStateException("Solo se pueden agregar productos publicados.");

        items.stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .ifPresentOrElse(
                        i -> i.aumentar(cantidad),
                        () -> items.add(new ItemCarrito(producto, cantidad)));
    }

    public void quitarProducto(UUID productoId) {
        boolean eliminado = items.removeIf(i -> i.getProducto().getId().equals(productoId));
        if (!eliminado)
            throw new IllegalArgumentException("Ese producto no está en el carrito.");
    }

    public void vaciar() {
        items.clear();
    }

    public UUID getId() { return id; }
    public Comprador getComprador() { return comprador; }
    public List<ItemCarrito> getItems() { return Collections.unmodifiableList(items); }
}