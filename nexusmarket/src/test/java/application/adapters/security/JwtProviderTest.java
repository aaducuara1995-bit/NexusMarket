package application.adapters.security;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Real JWT round trip with jjwt: sign, read back, and reject forged or expired tokens. */
class JwtProviderTest {

    private static final String SECRET = "TDEATDEATDEATDEATDEATDEATDEATDEA";

    private static User user() {
        return new User(new FullName("Ana", "Perez"), new Email("ana@mail.com"), new IdentityDocument("123"),
                UserRole.SUPERVISOR);
    }

    @Test
    void aSignedTokenGivesBackTheSameUser() {
        JwtProvider provider = new JwtProvider(SECRET, 3_600_000L);
        User original = user();

        String token = provider.generateToken(original);
        User read = provider.readUser(token).orElseThrow();

        assertEquals(3, token.split("\\.").length);
        assertEquals(original.getId(), read.getId());
        assertEquals(UserRole.SUPERVISOR, read.getRole());
        assertEquals(original.getEmail(), read.getEmail());
        assertEquals(3600, provider.getExpirationSeconds());
    }

    @Test
    void aTamperedOrForeignTokenIsRejected() {
        JwtProvider provider = new JwtProvider(SECRET, 3_600_000L);
        JwtProvider other = new JwtProvider("ANOTHERSECRETANOTHERSECRETANOTHER", 3_600_000L);
        String token = provider.generateToken(user());

        assertTrue(provider.readUser(token.substring(0, token.length() - 2) + "xx").isEmpty());
        assertTrue(other.readUser(token).isEmpty());
        assertTrue(provider.readUser("not-a-token").isEmpty());
    }

    @Test
    void anExpiredTokenIsRejected() {
        JwtProvider provider = new JwtProvider(SECRET, -1_000L);

        assertTrue(provider.readUser(provider.generateToken(user())).isEmpty());
    }
}