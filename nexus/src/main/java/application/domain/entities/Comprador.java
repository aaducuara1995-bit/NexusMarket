package application.domain.entities;

import application.domain.enums.EstadoComercial;
import application.domain.valueobjects.Direccion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Comprador {

    private final UUID id;
    private final UUID usuarioId;
    private Direccion direccionPrincipal;
    private final List<Direccion> direccionesAdicionales = new ArrayList<>();
    private EstadoComercial estadoComercial;

    public Comprador(UUID usuarioId, Direccion direccionPrincipal) {
        if (usuarioId == null)
            throw new IllegalArgumentException("El comprador debe estar asociado a un usuario.");
        if (direccionPrincipal == null)
            throw new IllegalArgumentException("El comprador debe tener una dirección principal.");

        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
        this.direccionPrincipal = direccionPrincipal;
        this.estadoComercial = EstadoComercial.Habilitado;
    }

    public void agregarDireccionAdicional(Direccion direccion) {
        if (direccion == null)
            throw new IllegalArgumentException("La dirección es obligatoria.");
        if (tieneDireccion(direccion))
            throw new IllegalStateException("Esa dirección ya está registrada.");

        direccionesAdicionales.add(direccion);
    }

    public void quitarDireccionAdicional(Direccion direccion) {
        if (!direccionesAdicionales.remove(direccion))
            throw new IllegalArgumentException("Esa dirección no está entre las adicionales.");
    }

    public void cambiarDireccionPrincipal(Direccion nueva) {
        if (nueva == null)
            throw new IllegalArgumentException("La dirección es obligatoria.");
        if (nueva.equals(direccionPrincipal))
            throw new IllegalStateException("Esa ya es la dirección principal.");

        direccionesAdicionales.remove(nueva);
        direccionesAdicionales.add(direccionPrincipal);
        direccionPrincipal = nueva;
    }

    public boolean tieneDireccion(Direccion direccion) {
        return direccionPrincipal.equals(direccion)
                || direccionesAdicionales.contains(direccion);
    }

    public void restringir() {
        if (estadoComercial == EstadoComercial.Restringido)
            throw new IllegalStateException("El comprador ya está restringido.");
        estadoComercial = EstadoComercial.Restringido;
    }

    public void habilitar() {
        if (estadoComercial == EstadoComercial.Habilitado)
            throw new IllegalStateException("El comprador ya está habilitado.");
        estadoComercial = EstadoComercial.Habilitado;
    }

    public boolean puedeComprar() {
        return estadoComercial == EstadoComercial.Habilitado;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
    public Direccion getDireccionPrincipal() { return direccionPrincipal; }
    public EstadoComercial getEstadoComercial() { return estadoComercial; }
    public List<Direccion> getDireccionesAdicionales() {
        return Collections.unmodifiableList(direccionesAdicionales);
    }
}
