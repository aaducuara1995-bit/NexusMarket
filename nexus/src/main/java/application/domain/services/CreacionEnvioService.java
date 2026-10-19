package application.domain.services;

import application.domain.entities.Envio;
import application.domain.entities.Pedido;
import application.domain.valueobjects.Direccion;

import java.util.List;

public class CreacionEnvioService {

    public Envio crear(Pedido pedido, Direccion direccionEntrega, List<Envio> existentes) {
        if (pedido == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (direccionEntrega == null)
            throw new IllegalArgumentException("La direcciÃ³n de entrega es obligatoria.");
        if (existentes == null)
            throw new IllegalArgumentException("La lista de envÃ­os existentes es obligatoria.");

        // Los envÃ­os son procesos logÃ­sticos solo para productos fÃ­sicos.
        if (pedido.esSoloDigital())
            throw new IllegalStateException("Los pedidos de productos digitales no llevan envÃ­o.");

        if (!pedido.getComprador().tieneDireccion(direccionEntrega))
            throw new IllegalArgumentException("La direcciÃ³n de entrega no pertenece al comprador.");

        boolean yaTieneEnvio = existentes.stream()
                .anyMatch(e -> e.getPedido().getId().equals(pedido.getId()));
        if (yaTieneEnvio)
            throw new IllegalStateException("El pedido ya tiene un envÃ­o.");

        // El propio envÃ­o exige que el pedido estÃ© pagado.
        return new Envio(pedido, direccionEntrega);
    }
}