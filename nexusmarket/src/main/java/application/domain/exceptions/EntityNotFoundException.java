package application.domain.exceptions;

/** Thrown when a use case looks for a record that does not exist in the repositories. */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity) {
        super(entity + " not found.");
    }
}