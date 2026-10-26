package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.InventoryReservationJpaEntity;
import application.domain.entities.Inventory;
import application.domain.entities.InventoryReservation;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper for InventoryReservation. The inventory is supplied when reading. */
public final class InventoryReservationJpaMapper {

    private InventoryReservationJpaMapper() {
    }

    public static InventoryReservationJpaEntity toEntity(InventoryReservation domain) {
        InventoryReservationJpaEntity entity = new InventoryReservationJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setOrderId(id(domain.getOrderId()));
        entity.setInventoryId(id(domain.getInventory().getId()));
        entity.setQuantity(domain.getQuantity());
        entity.setDate(domain.getDate());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    public static InventoryReservation toDomain(InventoryReservationJpaEntity entity, Inventory inventory) {
        return InventoryReservation.restore(uuid(entity.getId()), uuid(entity.getOrderId()), inventory,
                entity.getQuantity(), entity.getDate(), entity.getStatus());
    }
}