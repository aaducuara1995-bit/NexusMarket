package application.adapters.rest.exception;

/** The request does not say who is making it, or names a user that does not exist (HTTP 401). */
public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException(String message) {
        super(message);
    }
}