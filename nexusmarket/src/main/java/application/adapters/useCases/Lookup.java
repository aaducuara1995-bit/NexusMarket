package application.adapters.useCases;

import application.domain.exceptions.EntityNotFoundException;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/** Small helper the use cases share to reload domain objects from the repositories. */
final class Lookup {

    private Lookup() {
    }

    /**
     * The REST layer only knows identifiers, so every object that arrives at a use case
     * is reloaded from its repository. That guarantees it exists and that the use case
     * works with the stored state, not with a copy built by the caller.
     */
    static <T> T reload(T given, Function<T, UUID> idOf,
                        Function<UUID, Optional<T>> finder, String entity) {
        if (given == null)
            throw new IllegalArgumentException(entity + " is required.");
        return finder.apply(idOf.apply(given))
                .orElseThrow(() -> new EntityNotFoundException(entity));
    }
}