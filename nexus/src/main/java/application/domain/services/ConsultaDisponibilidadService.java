package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.enums.EstadoInventario;

import java.util.List;

public class ConsultaDisponibilidadService {

    public int disponibleDe(Producto producto, List<Inventario> inventarios) {
        validar(producto, inventarios);

        return inventarios.stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .filter(i -> i.getEstado() == EstadoInventario.Disponible)
                .mapToInt(Inventario::getDisponible)
                .sum();
    }

    public boolean hayDisponibilidad(Producto producto, int cantidad, List<Inventario> inventarios) {
        validar(producto, inventarios);
        if (cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");

        // Los productos digitales no requieren inventario (se entregan tras el pago).
        if (!producto.esFisico())
            return true;

        return disponibleDe(producto, inventarios) >= cantidad;
    }

    private static void validar(Producto producto, List<Inventario> inventarios) {
        if (producto == null)
            throw new IllegalArgumentException("El producto es obligatorio.");
        if (inventarios == null)
            throw new IllegalArgumentException("La lista de inventarios es obligatoria.");
    }
}