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

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
}