package application.adapters.security.mappers;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain User <-> JWT claims. The token carries everything needed to rebuild the user
 * (id in "sub", plus names, email, document, role and status), so each request gets
 * its User without going to the database. The password hash never goes in the token.
 */
public final class JwtClaimsMapper {

    public static final String FIRST_NAMES = "firstNames";
    public static final String LAST_NAMES = "lastNames";
    public static final String EMAIL = "email";
    public static final String DOCUMENT = "document";
    public static final String ROLE = "role";
    public static final String STATUS = "status";

    private JwtClaimsMapper() {
    }

    public static Map<String, Object> toClaims(User user) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put(FIRST_NAMES, user.getFullName().firstNames());
        claims.put(LAST_NAMES, user.getFullName().lastNames());
        claims.put(EMAIL, user.getEmail().value());
        claims.put(DOCUMENT, user.getDocument().value());
        claims.put(ROLE, user.getRole().name());
        claims.put(STATUS, user.getStatus().name());
        return claims;
    }

    public static User toDomain(String subject, Map<String, Object> claims) {
        return User.restore(UUID.fromString(subject),
                new FullName(text(claims, FIRST_NAMES), text(claims, LAST_NAMES)),
                new Email(text(claims, EMAIL)),
                new IdentityDocument(text(claims, DOCUMENT)),
                UserRole.valueOf(text(claims, ROLE)),
                UserStatus.valueOf(text(claims, STATUS)));
    }

    private static String text(Map<String, Object> claims, String name) {
        Object value = claims.get(name);
        if (value == null)
            throw new IllegalArgumentException("The token has no claim " + name + ".");
        return value.toString();
    }
}