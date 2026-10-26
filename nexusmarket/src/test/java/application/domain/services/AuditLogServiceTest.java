package application.domain.services;

import application.domain.entities.AuditLog;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogServiceTest {

    private final InMemoryRepositories.AuditLogs repository = new InMemoryRepositories.AuditLogs();
    private final RegisterAuditLogService register = new RegisterAuditLogService(repository);
    private final ConsultAuditLogService consult =
            new ConsultAuditLogService(new AuthorizationService(), repository);

    @Test
    void theEntryKeepsWhoDidWhatAndOnWhichRecord() {
        User seller = Fixtures.user(UserRole.SELLER);
        UUID productId = UUID.randomUUID();

        AuditLog entry = register.register(AuditOperation.PRODUCT_REGISTRATION, seller, productId,
                Map.of("name", "Mug"));

        assertEquals(seller.getId(), entry.getPerformedByUserId());
        assertEquals(UserRole.SELLER, entry.getPerformedByRole());
        assertEquals(productId, entry.getAffectedEntityId());
        assertEquals("Mug", entry.getDetails().get("name"));
        assertSame(entry, repository.saved.get(0));
    }

    @Test
    void theDetailsCannotBeChangedAfterwards() {
        Map<String, String> details = new HashMap<>(Map.of("status", "PAID"));
        AuditLog entry = new AuditLog(AuditOperation.ORDER_PAYMENT, Fixtures.user(UserRole.BUYER),
                UUID.randomUUID(), details);

        details.put("status", "CHANGED");

        assertEquals("PAID", entry.getDetails().get("status"));
        assertThrows(UnsupportedOperationException.class, () -> entry.getDetails().put("x", "y"));
    }

    @Test
    void anEntryNeedsOperationUserAndRecord() {
        User user = Fixtures.user(UserRole.ADMIN);
        assertThrows(IllegalArgumentException.class, () -> new AuditLog(null, user, UUID.randomUUID(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new AuditLog(AuditOperation.USER_BLOCKING, null, UUID.randomUUID(), null));
        assertThrows(IllegalArgumentException.class,
                () -> new AuditLog(AuditOperation.USER_BLOCKING, user, null, null));
    }

    @Test
    void supervisorAndAdminReadTheLogNewestFirst() {
        User admin = Fixtures.user(UserRole.ADMIN);
        UUID orderId = UUID.randomUUID();
        AuditLog older = AuditLog.restore(UUID.randomUUID(), AuditOperation.ORDER_CONFIRMATION,
                LocalDateTime.now().minusHours(2), admin.getId(), UserRole.BUYER, orderId, Map.of());
        repository.save(older);
        AuditLog newer = register.register(AuditOperation.ORDER_PAYMENT, admin, orderId, null);
        register.register(AuditOperation.USER_BLOCKING, admin, UUID.randomUUID(), null);

        List<AuditLog> ofOrder = consult.consultByEntity(Fixtures.user(UserRole.SUPERVISOR), orderId);

        assertEquals(List.of(newer, older), ofOrder);
        assertEquals(3, consult.consultAll(admin).size());
    }

    @Test
    void otherRolesCannotReadTheLog() {
        assertThrows(AccessDeniedException.class, () -> consult.consultAll(Fixtures.user(UserRole.BUYER)));
        assertThrows(AccessDeniedException.class,
                () -> consult.consultAll(Fixtures.user(UserRole.LOGISTIC_OPERATOR)));
    }
}