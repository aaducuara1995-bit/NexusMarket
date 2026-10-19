package application.domain.ports.out;

import application.domain.entities.Comprador;

import java.util.UUID;

public interface CompradorRepositorioPort {

    boolean existePorUsuarioId(UUID usuarioId);

    void guardar(Comprador comprador);
}