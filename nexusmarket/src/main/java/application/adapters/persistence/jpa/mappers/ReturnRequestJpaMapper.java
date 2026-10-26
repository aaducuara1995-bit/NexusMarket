package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import application.domain.entities.Order;
import application.domain.entities.ReturnRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper for ReturnRequest. The order is supplied when reading. */
public final class ReturnRequestJpaMapper {

    private ReturnRequestJpaMapper() {
    }

    public static ReturnRequestJpaEntity toEntity(ReturnRequest domain) {
        ReturnRequestJpaEntity entity = new ReturnRequestJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setOrderId(id(domain.getOrder().getId()));
        entity.setReason(domain.getReason());
        entity.setStatus(domain.getStatus());
        Map<String, Integer> restocked = new HashMap<>();
        domain.getRestockedUnits().forEach((productId, units) -> restocked.put(id(productId), units));
        entity.setRestockedUnits(restocked);
        return entity;
    }

    public static ReturnRequest toDomain(ReturnRequestJpaEntity entity, Order order) {
        Map<UUID, Integer> restocked = new HashMap<>();
        entity.getRestockedUnits().forEach((productId, units) -> restocked.put(uuid(productId), units));
        return ReturnRequest.restore(uuid(entity.getId()), order, entity.getReason(),
                entity.getStatus(), restocked);
    }
}