package application.domain.entities;

import application.domain.enums.ClasificacionBodega;
import application.domain.enums.TipoBodega;
import application.domain.valueobjects.Direccion;

import java.util.UUID;

public class Bodega {

    private final UUID id;
    private final String nombre;
    private final TipoBodega tipo;
    private final Direccion direccion;
    private final ClasificacionBodega clasificacion;
    private final UUID vendedorId;

    private Bodega(String nombre, TipoBodega tipo, Direccion direccion,
                   ClasificacionBodega clasificacion, UUID vendedorId) {
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
        this.clasificacion = clasificacion;
        this.vendedorId = vendedorId;
    }

    public static Bodega delMarketplace(String nombre, TipoBodega tipo, Direccion direccion) {
        return new Bodega(nombre, tipo, direccion, ClasificacionBodega.Marketplace, null);
    }

    public static Bodega deVendedor(UUID vendedorId, String nombre,
                                    TipoBodega tipo, Direccion direccion) {
        if (vendedorId == null)
            throw new IllegalArgumentException("Una bodega de vendedor debe tener propietario.");
        return new Bodega(nombre, tipo, direccion, ClasificacionBodega.Vendedor, vendedorId);
    }

    public boolean esDigital() {
        return tipo == TipoBodega.Digital;
    }

    public boolean esDelMarketplace() {
        return clasificacion == ClasificacionBodega.Marketplace;
    }

    public boolean perteneceA(UUID idVendedor) {
        return clasificacion == ClasificacionBodega.Vendedor
                && vendedorId.equals(idVendedor);
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoBodega getTipo() { return tipo; }
    public Direccion getDireccion() { return direccion; }
    public ClasificacionBodega getClasificacion() { return clasificacion; }
    public UUID getVendedorId() { return vendedorId; }
}
