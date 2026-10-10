package application.domain.entities;

import application.domain.enums.TipoBodega;
import application.domain.valueobjects.Direccion;

import java.util.UUID;

public class Bodega {

    private final UUID id;
    private final String nombre;
    private final TipoBodega tipo;
    private final Direccion direccion;

    public Bodega(String nombre, TipoBodega tipo, Direccion direccion) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre de la bodega no puede estar vacío.");
        if (tipo == null)
            throw new IllegalArgumentException("El tipo de bodega es obligatorio.");
        if (tipo == TipoBodega.Fisica && direccion == null)
            throw new IllegalArgumentException("Una bodega física debe tener dirección.");
        if (tipo == TipoBodega.Digital && direccion != null)
            throw new IllegalArgumentException("Una bodega digital no tiene dirección física.");

        this.id = UUID.randomUUID();
        this.nombre = nombre.trim();
        this.tipo = tipo;
        this.direccion = direccion;
    }

    public boolean esDigital() {
        return tipo == TipoBodega.Digital;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoBodega getTipo() { return tipo; }
    public Direccion getDireccion() { return direccion; }
}