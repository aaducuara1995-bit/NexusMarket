package application.adapters.security;

import application.adapters.security.mappers.JwtClaimsMapper;
import application.domain.entities.User;
import application.domain.ports.out.TokenServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Issues and reads the JWT (HS256, signed with jwt.secret). Implements TokenServicePort
 * for the login service; the filter uses readUser to rebuild the signed-in user.
 *
 * Academic trade-off, the same as the reference project: role and status travel in the
 * token, so a change (for example, blocking a user) takes effect on their next login or
 * when the token expires (jwt.expiration-ms, one hour by default).
 */
@Component
public class JwtProvider implements TokenServicePort {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtProvider(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(JwtClaimsMapper.toClaims(user))
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();
    }

    /** The user inside a valid token, or empty if the token is forged, malformed or expired. */
    public Optional<User> readUser(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            return Optional.of(JwtClaimsMapper.toDomain(claims.getSubject(), claims));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}