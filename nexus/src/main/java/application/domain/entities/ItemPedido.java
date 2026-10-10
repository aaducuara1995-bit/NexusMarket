package application.domain.entities;

import application.domain.valueobjects.Dinero;

import java.math.BigDecimal;
import java.util.UUID;

public class ItemPedido {

    private final UUID productoId;
    private final String nombreProducto;
    private final int cantidad;
    private final Dinero precioUnitario;

    ItemPedido(Producto producto, int cantidad) {
        this.productoId = producto.getId();
        this.nombreProducto = producto.getNombre();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
    }

    public Dinero subtotal() {
        return new Dinero(
                precioUnitario.monto().multiply(BigDecimal.valueOf(cantidad)),
                precioUnitario.moneda());
    }

    public UUID getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; }
    public Dinero getPrecioUnitario() { return precioUnitario; }
}