package application.domain.entities;

public class ItemCarrito {

    private final Producto producto;
    private int cantidad;

    ItemCarrito(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    void aumentar(int unidades) {
        this.cantidad += unidades;
    }

    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
}