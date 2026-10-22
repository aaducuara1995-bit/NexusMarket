package application.domain.ports.in;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;

import java.util.List;

/** Puerto de entrada: consulta cuántas unidades de un producto hay disponibles entre todas las bodegas. */
public interface ConsultarDisponibilidadUseCase {

    int disponibleDe(Producto producto, List<Inventario> inventarios);

    boolean hayDisponibilidad(Producto producto, int cantidad, List<Inventario> inventarios);
}