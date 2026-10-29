package application.adapters.security;

import application.adapters.security.dtos.AuthenticatedUserPrincipal;
import application.adapters.security.mappers.JwtClaimsMapper;
import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Security pieces that do not need a running server: claims, principal, filter and BCrypt. */
class SecurityAdaptersTest {

    private static final String SECRET = "TDEATDEATDEATDEATDEATDEATDEATDEA";

    private static User user(UserRole role) {
        return new User(new FullName("Ana", "Perez"), new Email("ana@mail.com"), new IdentityDocument("123"), role);
    }

    @Test
    void theClaimsRebuildTheSameUserWithoutThePassword() {
        User original = user(UserRole.SELLER);
        original.assignPasswordHash("secret-hash");

        Map<String, Object> claims = JwtClaimsMapper.toClaims(original);
        User rebuilt = JwtClaimsMapper.toDomain(original.getId().toString(), claims);

        assertEquals(original.getId(), rebuilt.getId());
        assertEquals(original.getEmail(), rebuilt.getEmail());
        assertEquals(UserRole.SELLER, rebuilt.getRole());
        assertEquals(UserStatus.ACTIVE, rebuilt.getStatus());
        assertFalse(claims.containsValue("secret-hash"));
        assertFalse(rebuilt.hasPassword());
    }

    @Test
    void aTokenWithoutAllItsClaimsIsRejected() {
        User original = user(UserRole.BUYER);
        Map<String, Object> claims = JwtClaimsMapper.toClaims(original);
        claims.remove(JwtClaimsMapper.ROLE);

        assertThrows(IllegalArgumentException.class,
                () -> JwtClaimsMapper.toDomain(original.getId().toString(), claims));
    }

    @Test
    void thePrincipalCarriesTheRoleThatSecurityConfigChecks() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(user(UserRole.LOGISTIC_OPERATOR));

        assertEquals("ROLE_LOGISTIC_OPERATOR", principal.getAuthorities().iterator().next().getAuthority());
        assertEquals("ana@mail.com", principal.getUsername());
        assertNull(principal.getPassword());
        assertTrue(principal.isEnabled());
    }

    @Test
    void bcryptNeverStoresThePasswordAsItIs() {
        BCryptPasswordServiceAdapter passwords = new BCryptPasswordServiceAdapter(new BCryptPasswordEncoder());

        String hash = passwords.encrypt("secret123");

        assertNotEquals("secret123", hash);
        assertTrue(passwords.matches("secret123", hash));
        assertFalse(passwords.matches("other-one", hash));
        assertFalse(passwords.matches("secret123", null));
    }

    @Test
    void theFilterAuthenticatesOnlyAValidTokenOfAnActiveUser() throws Exception {
        User active = user(UserRole.BUYER);
        User blocked = user(UserRole.BUYER);
        blocked.block();
        JwtProvider provider = new JwtProvider(SECRET, 3_600_000L) {
            @Override
            public Optional<User> readUser(String token) {
                return switch (token) {
                    case "good" -> Optional.of(active);
                    case "blocked" -> Optional.of(blocked);
                    default -> Optional.empty();
                };
            }
        };
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);

        assertSame(active, authenticatedAfter(filter, "Bearer good"));
        assertNull(authenticatedAfter(filter, "Bearer blocked"));
        assertNull(authenticatedAfter(filter, "Bearer forged"));
        assertNull(authenticatedAfter(filter, "good"));
        assertNull(authenticatedAfter(filter, null));
    }

    private static User authenticatedAfter(JwtAuthenticationFilter filter, String authorization) throws Exception {
        SecurityContextHolder.clearContext();
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> "getHeader".equals(method.getName()) ? authorization : null);
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(), new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> null);
        boolean[] continued = {false};
        filter.doFilterInternal(request, response, (req, res) -> continued[0] = true);
        assertTrue(continued[0], "The filter must always let the request continue.");
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.clearContext();
        return authentication == null ? null : ((AuthenticatedUserPrincipal) authentication.getPrincipal()).getUser();
    }
}