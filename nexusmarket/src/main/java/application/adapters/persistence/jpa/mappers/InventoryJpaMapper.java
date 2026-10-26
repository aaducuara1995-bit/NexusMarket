package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.InventoryJpaEntity;
import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.Warehouse;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper for Inventory. Product and warehouse are stored by id and supplied when reading. */
public final class InventoryJpaMapper {

    private InventoryJpaMapper() {
    }

    public static InventoryJpaEntity toEntity(Inventory domain) {
        InventoryJpaEntity entity = new InventoryJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setProductId(id(domain.getProduct().getId()));
        entity.setWarehouseId(id(domain.getWarehouse().getId()));
        entity.setQuantity(domain.getQuantity());
        entity.setReserved(domain.getReserved());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    public static Inventory toDomain(InventoryJpaEntity entity, Product product, Warehouse warehouse) {
        return Inventory.restore(uuid(entity.getId()), product, warehouse,
                entity.getQuantity(), entity.getReserved(), entity.getStatus());
    }
}