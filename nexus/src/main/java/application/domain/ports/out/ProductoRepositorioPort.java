package application.domain.ports.out;

import application.domain.entities.Producto;

import java.util.UUID;

public interface ProductoRepositorioPort {

    boolean existeNombreParaVendedor(UUID vendedorId, String nombre);

    void guardar(Producto producto);
}