package application.domain.entities;

import application.domain.enums.EstadoEnvio;
import application.domain.enums.EstadoPedido;
import application.domain.valueobjects.Direccion;

import java.util.UUID;

public class Envio {

    private final UUID id;
    private final Pedido pedido;
    private final Direccion direccionEntrega;
    private EstadoEnvio estado;

    public Envio(Pedido pedido, Direccion direccionEntrega) {
        if (pedido == null)
            throw new IllegalArgumentException("El envío debe estar asociado a un pedido.");
        if (direccionEntrega == null)
            throw new IllegalArgumentException("La dirección de entrega es obligatoria.");
        if (pedido.getEstado() != EstadoPedido.Pagado)
            throw new IllegalStateException("Solo se puede crear el envío de un pedido pagado.");

        this.id = UUID.randomUUID();
        this.pedido = pedido;
        this.direccionEntrega = direccionEntrega;
        this.estado = EstadoEnvio.Pendiente;
    }

    public void despachar() {
        if (estado != EstadoEnvio.Pendiente)
            throw new IllegalStateException("Solo se puede despachar un envío pendiente.");
        estado = EstadoEnvio.Despachado;
    }

    public void entregar() {
        if (estado != EstadoEnvio.Despachado)
            throw new IllegalStateException("Solo se puede entregar un envío despachado.");
        estado = EstadoEnvio.Entregado;
    }

    public UUID getId() { return id; }
    public Pedido getPedido() { return pedido; }
    public Direccion getDireccionEntrega() { return direccionEntrega; }
    public EstadoEnvio getEstado() { return estado; }
}