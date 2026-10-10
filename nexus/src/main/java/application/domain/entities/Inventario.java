package application.domain.entities;

import application.domain.enums.EstadoInventario;

import java.util.UUID;

public class Inventario {

    private final UUID id;
    private final Producto producto;
    private final Bodega bodega;
    private int cantidad;
    private int reservado;
    private EstadoInventario estado;

    public Inventario(Producto producto, Bodega bodega, int cantidadInicial) {
        if (producto == null || bodega == null)
            throw new IllegalArgumentException("Producto y bodega son obligatorios.");
        if (cantidadInicial < 0)
            throw new IllegalArgumentException("El inventario no puede ser negativo.");

        this.id = UUID.randomUUID();
        this.producto = producto;
        this.bodega = bodega;
        this.cantidad = cantidadInicial;
        this.reservado = 0;
        this.estado = EstadoInventario.Disponible;
    }

    public int getDisponible() {
        return cantidad - reservado;
    }

    public void ingresar(int unidades) {
        validarPositiva(unidades);
        cantidad += unidades;
    }

    public void reservar(int unidades) {
        validarPositiva(unidades);
        if (estado != EstadoInventario.Disponible)
            throw new IllegalStateException("No se puede reservar inventario que no está disponible.");
        if (getDisponible() < unidades)
            throw new IllegalStateException("No hay unidades suficientes para reservar.");

        reservado += unidades;
    }

    public void liberarReserva(int unidades) {
        validarPositiva(unidades);
        if (unidades > reservado)
            throw new IllegalArgumentException("No hay tantas unidades reservadas.");

        reservado -= unidades;
    }

    public void confirmarSalida(int unidades) {
        validarPositiva(unidades);
        if (unidades > reservado)
            throw new IllegalArgumentException("La salida debe corresponder a una reserva vigente.");

        reservado -= unidades;
        cantidad -= unidades;
    }

    private static void validarPositiva(int unidades) {
        if (unidades <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
    }

    public UUID getId() { return id; }
    public Producto getProducto() { return producto; }
    public Bodega getBodega() { return bodega; }
    public int getCantidad() { return cantidad; }
    public int getReservado() { return reservado; }
    public EstadoInventario getEstado() { return estado; }
}