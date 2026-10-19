package application.domain.services;

import application.domain.entities.Factura;
import application.domain.entities.Pedido;

import java.util.List;

public class EmisionFacturaService {

    public Factura emitir(Pedido pedido, List<Factura> existentes) {
        if (pedido == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (existentes == null)
            throw new IllegalArgumentException("La lista de facturas existentes es obligatoria.");

        boolean yaFacturado = existentes.stream()
                .anyMatch(f -> f.getPedido().getId().equals(pedido.getId()));
        if (yaFacturado)
            throw new IllegalStateException("El pedido ya fue facturado.");

        // La propia factura exige que el pedido estÃ© pagado y toma el total del pedido.
        return new Factura(pedido);
    }
}