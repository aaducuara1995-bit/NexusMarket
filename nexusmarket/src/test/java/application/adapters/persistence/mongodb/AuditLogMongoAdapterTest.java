package application.adapters.persistence.mongodb;

import application.adapters.persistence.FakeSpringDataRepository;
import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import application.adapters.persistence.mongodb.mappers.AuditLogMongoMapper;
import application.adapters.persistence.mongodb.repositories.AuditLogMongoRepository;
import application.domain.entities.AuditLog;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.enums.UserRole;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogMongoAdapterTest {

    private final AuditLogMongoRepository documents = FakeSpringDataRepository.of(AuditLogMongoRepository.class);
    private final AuditLogMongoAdapter adapter = new AuditLogMongoAdapter(documents);

    private static User user(UserRole role, String email) {
        return new User(new FullName("Ana", "Perez"), new Email(email), new IdentityDocument("123"), role);
    }

    @Test
    void theDocumentStoresEnumsAsNamesAndIdsAsText() {
        User operator = user(UserRole.LOGISTIC_OPERATOR, "op@mail.com");
        UUID orderId = UUID.randomUUID();
        AuditLog entry = new AuditLog(AuditOperation.ORDER_SHIPMENT, operator, orderId, Map.of("shipment", "S-1"));

        AuditLogDocument document = AuditLogMongoMapper.toDocument(entry);

        assertEquals("ORDER_SHIPMENT", document.getOperation());
        assertEquals("LOGISTIC_OPERATOR", document.getPerformedByRole());
        assertEquals(orderId.toString(), document.getAffectedEntityId());
        assertEquals("S-1", document.getDetails().get("shipment"));
    }

    @Test
    void anEntryIsReadBackExactlyAsItWasSaved() {
        AuditLog entry = new AuditLog(AuditOperation.ORDER_PAYMENT, user(UserRole.BUYER, "b@mail.com"),
                UUID.randomUUID(), Map.of("status", "PAID", "invoice", "F-9"));

        adapter.save(entry);
        AuditLog read = adapter.findAll().get(0);

        assertEquals(entry.getId(), read.getId());
        assertEquals(entry.getOperation(), read.getOperation());
        assertEquals(entry.getDate(), read.getDate());
        assertEquals(entry.getPerformedByUserId(), read.getPerformedByUserId());
        assertEquals(entry.getPerformedByRole(), read.getPerformedByRole());
        assertEquals(entry.getAffectedEntityId(), read.getAffectedEntityId());
        assertEquals(entry.getDetails(), read.getDetails());
    }

    @Test
    void entriesAreFoundByUserAndByAffectedRecord() {
        User admin = user(UserRole.ADMIN, "admin@mail.com");
        User buyer = user(UserRole.BUYER, "buyer@mail.com");
        UUID orderId = UUID.randomUUID();
        adapter.save(new AuditLog(AuditOperation.ORDER_CONFIRMATION, buyer, orderId, null));
        adapter.save(new AuditLog(AuditOperation.ORDER_PAYMENT, buyer, orderId, null));
        adapter.save(new AuditLog(AuditOperation.USER_BLOCKING, admin, buyer.getId(), null));

        List<AuditLog> byBuyer = adapter.findByUser(buyer.getId());
        List<AuditLog> ofOrder = adapter.findByAffectedEntity(orderId);
        List<AuditLog> ofBuyer = adapter.findByAffectedEntity(buyer.getId());

        assertEquals(2, byBuyer.size());
        assertEquals(2, ofOrder.size());
        assertEquals(AuditOperation.USER_BLOCKING, ofBuyer.get(0).getOperation());
    }
}