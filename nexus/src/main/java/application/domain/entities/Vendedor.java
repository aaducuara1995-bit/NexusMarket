package application.domain.entities;

import java.util.UUID;

public class Vendedor {

    private final UUID id;
    private final UUID usuarioId;

    public Vendedor(UUID usuarioId) {
        if (usuarioId == null)
            throw new IllegalArgumentException("El vendedor debe estar asociado a un usuario.");

        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
}
