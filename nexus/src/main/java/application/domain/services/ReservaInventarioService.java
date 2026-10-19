package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Producto;
import application.domain.entities.ReservaInventario;
import application.domain.enums.EstadoInventario;
import application.domain.enums.TipoMovimientoInventario;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ReservaInventarioService {

    public record Resultado(List<ReservaInventario> reservas,
                            List<MovimientoInventario> movimientos) {
    }

    public Resultado reservar(UUID pedidoId, Producto producto, int cantidad,
                              List<Inventario> inventarios) {
        if (pedidoId == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (producto == null)
            throw new IllegalArgumentException("El producto es obligatorio.");
        if (inventarios == null)
            throw new IllegalArgumentException("La lista de inventarios es obligatoria.");
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");

        // Los productos digitales no consumen inventario.
        if (!producto.esFisico())
            return new Resultado(List.of(), List.of());

        List<Inventario> candidatos = inventarios.stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .filter(i -> i.getEstado() == EstadoInventario.Disponible)
                .filter(i -> i.getDisponible() > 0)
                .sorted(Comparator.comparingInt(Inventario::getDisponible).reversed())
                .toList();

        int totalDisponible = candidatos.stream().mapToInt(Inventario::getDisponible).sum();
        if (totalDisponible < cantidad)
            throw new IllegalStateException("No hay existencias suficientes de "
                    + producto.getNombre() + " para reservar " + cantidad + " unidades.");

        List<ReservaInventario> reservas = new ArrayList<>();
        List<MovimientoInventario> movimientos = new ArrayList<>();
        int pendiente = cantidad;

        try {
            for (Inventario inventario : candidatos) {
                if (pendiente == 0)
                    break;

                int tomar = Math.min(pendiente, inventario.getDisponible());
                reservas.add(ReservaInventario.crear(pedidoId, inventario, tomar));
                movimientos.add(new MovimientoInventario(
                        inventario, TipoMovimientoInventario.Reserva, tomar,
                        "Reserva para el pedido " + pedidoId));
                pendiente -= tomar;
            }
        } catch (RuntimeException e) {
            reservas.forEach(ReservaInventario::liberar);
            throw e;
        }

        return new Resultado(reservas, movimientos);
    }
}