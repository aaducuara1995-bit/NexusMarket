package application.domain.entities;

/** Shared check for the restore factories: a stored record must bring all its required data. */
final class Restore {

    private Restore() {
    }

    static void require(Object value, String field) {
        if (value == null)
            throw new IllegalArgumentException("Cannot restore without " + field + ".");
    }
}