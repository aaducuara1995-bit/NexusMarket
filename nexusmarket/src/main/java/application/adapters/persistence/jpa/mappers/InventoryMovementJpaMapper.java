package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.InventoryMovementJpaEntity;
import application.domain.entities.InventoryMovement;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper between the InventoryMovement domain entity and its JPA entity. */
public final class InventoryMovementJpaMapper {

    private InventoryMovementJpaMapper() {
    }

    public static InventoryMovementJpaEntity toEntity(InventoryMovement domain) {
        InventoryMovementJpaEntity entity = new InventoryMovementJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setInventoryId(id(domain.getInventoryId()));
        entity.setProductId(id(domain.getProductId()));
        entity.setWarehouseId(id(domain.getWarehouseId()));
        entity.setType(domain.getType());
        entity.setQuantity(domain.getQuantity());
        entity.setReason(domain.getReason());
        entity.setDate(domain.getDate());
        return entity;
    }

    public static InventoryMovement toDomain(InventoryMovementJpaEntity entity) {
        return InventoryMovement.restore(uuid(entity.getId()), uuid(entity.getInventoryId()),
                uuid(entity.getProductId()), uuid(entity.getWarehouseId()), entity.getType(),
                entity.getQuantity(), entity.getReason(), entity.getDate());
    }
}