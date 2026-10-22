package application.domain.services;

import application.domain.ports.in.GenerarReembolsoUseCase;

import application.domain.entities.Devolucion;
import application.domain.entities.Pedido;
import application.domain.entities.Reembolso;

import java.util.List;

public class GeneracionReembolsoService implements GenerarReembolsoUseCase {

    public Reembolso generar(Devolucion devolucion, List<Reembolso> existentes) {
        if (devolucion == null)
            throw new IllegalArgumentException("La devolución es obligatoria.");
        if (existentes == null)
            throw new IllegalArgumentException("La lista de reembolsos existentes es obligatoria.");

        if (!devolucion.estaAprobada())
            throw new IllegalStateException("Solo se genera el reembolso de una devolución aprobada.");

        Pedido pedido = devolucion.getPedido();
        boolean yaReembolsado = existentes.stream()
                .anyMatch(r -> r.getPedido().getId().equals(pedido.getId()));
        if (yaReembolsado)
            throw new IllegalStateException("El pedido ya tiene un reembolso.");

        // La devolución cubre el pedido completo, así que se reembolsa su total.
        return new Reembolso(pedido, pedido.calcularTotal());
    }
}