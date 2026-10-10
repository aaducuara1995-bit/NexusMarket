package application.domain.entities;

import java.util.UUID;

public class Comprador {

    private final UUID id;
    private final UUID usuarioId;

    public Comprador(UUID usuarioId) {
        if (usuarioId == null)
            throw new IllegalArgumentException("El comprador debe estar asociado a un usuario.");

        this.id = UUID.randomUUID();
        this.usuarioId = usuarioId;
    }

    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuarioId; }
}