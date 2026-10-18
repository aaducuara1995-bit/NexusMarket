package application.domain.entities;

import application.domain.enums.EstadoProducto;
import application.domain.enums.TipoProducto;
import application.domain.valueobjects.Dinero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Producto {

    private final UUID id;
    private final UUID vendedorId;
    private final String nombre;
    private final String descripcion;
    private final TipoProducto tipo;
    private final List<String> variantes = new ArrayList<>();
    private Dinero precio;
    private EstadoProducto estado;

    public Producto(UUID vendedorId, String nombre, String descripcion,
                    TipoProducto tipo, Dinero precio) {
        if (vendedorId == null)
            throw new IllegalArgumentException("El producto debe pertenecer a un vendedor.");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        if (tipo == null)
            throw new IllegalArgumentException("El tipo de producto es obligatorio.");
        validarPrecio(precio);

        this.id = UUID.randomUUID();
        this.vendedorId = vendedorId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.precio = precio;
        this.estado = EstadoProducto.Suspendido;
    }

    public void agregarVariante(String variante) {
        if (variante == null || variante.isBlank())
            throw new IllegalArgumentException("La variante no puede estar vacía.");
        if (variantes.contains(variante))
            throw new IllegalStateException("Esa variante ya existe en el producto.");

        variantes.add(variante);
    }

    public void cambiarPrecio(Dinero nuevoPrecio) {
        validarPrecio(nuevoPrecio);
        this.precio = nuevoPrecio;
    }

    public void publicar() {
        if (estado == EstadoProducto.Descontinuado)
            throw new IllegalStateException("Un producto descontinuado no puede volver a publicarse.");
        if (estado == EstadoProducto.Publicado)
            throw new IllegalStateException("El producto ya está publicado.");

        estado = EstadoProducto.Publicado;
    }

    public void suspender() {
        if (estado == EstadoProducto.Descontinuado)
            throw new IllegalStateException("Un producto descontinuado no puede suspenderse.");
        if (estado == EstadoProducto.Suspendido)
            throw new IllegalStateException("El producto ya está suspendido.");

        estado = EstadoProducto.Suspendido;
    }

    public void descontinuar() {
        if (estado == EstadoProducto.Descontinuado)
            throw new IllegalStateException("El producto ya está descontinuado.");

        estado = EstadoProducto.Descontinuado;
    }

    public boolean estaPublicado() {
        return estado == EstadoProducto.Publicado;
    }

    public boolean esFisico() {
        return tipo == TipoProducto.Fisico;
    }

    public boolean perteneceA(UUID idVendedor) {
        return vendedorId.equals(idVendedor);
    }

    private static void validarPrecio(Dinero precio) {
        if (precio == null || precio.monto().signum() <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
    }

    public UUID getId() { return id; }
    public UUID getVendedorId() { return vendedorId; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public TipoProducto getTipo() { return tipo; }
    public Dinero getPrecio() { return precio; }
    public EstadoProducto getEstado() { return estado; }
    public List<String> getVariantes() { return Collections.unmodifiableList(variantes); }
}
