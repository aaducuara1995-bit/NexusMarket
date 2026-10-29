package application.adapters.security;

import application.domain.ports.out.PasswordServicePort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Implements PasswordServicePort with BCrypt: the database only ever stores the hash. */
@Component
public class BCryptPasswordServiceAdapter implements PasswordServicePort {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordServiceAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encrypt(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null)
            return false;
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}