package application.domain.services;

import application.domain.ports.in.CrearEnvioUseCase;

import application.domain.entities.Envio;
import application.domain.entities.Pedido;
import application.domain.valueobjects.Direccion;

import java.util.List;

public class CreacionEnvioService implements CrearEnvioUseCase {

    public Envio crear(Pedido pedido, Direccion direccionEntrega, List<Envio> existentes) {
        if (pedido == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (direccionEntrega == null)
            throw new IllegalArgumentException("La dirección de entrega es obligatoria.");
        if (existentes == null)
            throw new IllegalArgumentException("La lista de envíos existentes es obligatoria.");

        // Los envíos son procesos logísticos solo para productos físicos.
        if (pedido.esSoloDigital())
            throw new IllegalStateException("Los pedidos de productos digitales no llevan envío.");

        if (!pedido.getComprador().tieneDireccion(direccionEntrega))
            throw new IllegalArgumentException("La dirección de entrega no pertenece al comprador.");

        boolean yaTieneEnvio = existentes.stream()
                .anyMatch(e -> e.getPedido().getId().equals(pedido.getId()));
        if (yaTieneEnvio)
            throw new IllegalStateException("El pedido ya tiene un envío.");

        // El propio envío exige que el pedido esté pagado.
        return new Envio(pedido, direccionEntrega);
    }
}