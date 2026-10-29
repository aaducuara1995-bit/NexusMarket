package application.domain.exceptions;

/**
 * Login rejected. The message is the same whether the email does not exist or the
 * password is wrong, so the answer does not reveal which emails are registered.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }
}