package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.domain.entities.Warehouse;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.toAddress;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.toEmbeddable;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper between the Warehouse domain entity and its JPA entity. */
public final class WarehouseJpaMapper {

    private WarehouseJpaMapper() {
    }

    public static WarehouseJpaEntity toEntity(Warehouse domain) {
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setAddress(toEmbeddable(domain.getAddress()));
        entity.setOwnership(domain.getOwnership());
        entity.setSellerId(id(domain.getSellerId()));
        return entity;
    }

    public static Warehouse toDomain(WarehouseJpaEntity entity) {
        return Warehouse.restore(uuid(entity.getId()), entity.getName(), entity.getType(),
                toAddress(entity.getAddress()), entity.getOwnership(), uuid(entity.getSellerId()));
    }
}