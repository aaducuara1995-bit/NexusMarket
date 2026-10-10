package application.domain.entities;

import application.domain.enums.EstadoProducto;
import application.domain.enums.TipoProducto;

import java.math.BigDecimal;
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
    private BigDecimal precio;
    private EstadoProducto estado;

    public Producto(UUID vendedorId, String nombre, String descripcion,
                    TipoProducto tipo, BigDecimal precio) {
        if (vendedorId == null)
            throw new IllegalArgumentException("El producto debe pertenecer a un vendedor.");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
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

    public void cambiarPrecio(BigDecimal nuevoPrecio) {
        validarPrecio(nuevoPrecio);
        this.precio = nuevoPrecio;
    }

    public void publicar() {
        if (estado == EstadoProducto.Descontinuado)
            throw new IllegalStateException("Un producto descontinuado no puede volver a publicarse.");

        estado = EstadoProducto.Publicado;
    }

    public void descontinuar() {
        estado = EstadoProducto.Descontinuado;
    }

    private static void validarPrecio(BigDecimal precio) {
        if (precio == null || precio.signum() <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
    }

    public UUID getId() { return id; }
    public UUID getVendedorId() { return vendedorId; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public TipoProducto getTipo() { return tipo; }
    public BigDecimal getPrecio() { return precio; }
    public EstadoProducto getEstado() { return estado; }
    public List<String> getVariantes() { return Collections.unmodifiableList(variantes); }
}