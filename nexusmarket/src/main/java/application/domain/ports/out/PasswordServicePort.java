package application.domain.ports.out;

/** Hashes and checks passwords. Implemented with BCrypt in the security adapters. */
public interface PasswordServicePort {
    String encrypt(String rawPassword);
    boolean matches(String rawPassword, String passwordHash);
}