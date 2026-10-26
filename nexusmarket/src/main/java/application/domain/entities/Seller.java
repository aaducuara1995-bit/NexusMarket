package application.domain.entities;

import java.util.UUID;

public class Seller {

    private final UUID id;
    private final UUID userId;

    public Seller(UUID userId) {
        if (userId == null)
            throw new IllegalArgumentException("The seller must be linked to a user.");

        this.id = UUID.randomUUID();
        this.userId = userId;
    }

    private Seller(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
    }

    /** Rebuilds a seller saved by a persistence adapter. It is not for creating new ones. */
    public static Seller restore(UUID id, UUID userId) {
        Restore.require(id, "id");
        Restore.require(userId, "userId");
        return new Seller(id, userId);
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
}