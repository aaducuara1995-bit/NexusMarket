package application.adapters.persistence.mongodb.repositories;

import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/** Spring Data MongoDB repository for AuditLogDocument. Query methods are derived from their names. */
public interface AuditLogMongoRepository extends MongoRepository<AuditLogDocument, String> {

    List<AuditLogDocument> findByPerformedByUserId(String performedByUserId);

    List<AuditLogDocument> findByAffectedEntityId(String affectedEntityId);
}