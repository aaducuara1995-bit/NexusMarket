package application.domain.ports.out;

import application.domain.entities.Vendedor;

import java.util.UUID;

public interface VendedorRepositorioPort {

    boolean existePorUsuarioId(UUID usuarioId);

    void guardar(Vendedor vendedor);
}