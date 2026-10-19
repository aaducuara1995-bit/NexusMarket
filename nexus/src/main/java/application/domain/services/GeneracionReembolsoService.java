package application.domain.services;

import application.domain.entities.Devolucion;
import application.domain.entities.Pedido;
import application.domain.entities.Reembolso;

import java.util.List;

public class GeneracionReembolsoService {

    public Reembolso generar(Devolucion devolucion, List<Reembolso> existentes) {
        if (devolucion == null)
            throw new IllegalArgumentException("La devoluciÃ³n es obligatoria.");
        if (existentes == null)
            throw new IllegalArgumentException("La lista de reembolsos existentes es obligatoria.");

        if (!devolucion.estaAprobada())
            throw new IllegalStateException("Solo se genera el reembolso de una devoluciÃ³n aprobada.");

        Pedido pedido = devolucion.getPedido();
        boolean yaReembolsado = existentes.stream()
                .anyMatch(r -> r.getPedido().getId().equals(pedido.getId()));
        if (yaReembolsado)
            throw new IllegalStateException("El pedido ya tiene un reembolso.");

        // La devoluciÃ³n cubre el pedido completo, asÃ­ que se reembolsa su total.
        return new Reembolso(pedido, pedido.calcularTotal());
    }
}