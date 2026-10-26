package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import application.domain.entities.Order;
import application.domain.entities.Shipment;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.toAddress;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.toEmbeddable;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper for Shipment. The order is supplied when reading. */
public final class ShipmentJpaMapper {

    private ShipmentJpaMapper() {
    }

    public static ShipmentJpaEntity toEntity(Shipment domain) {
        ShipmentJpaEntity entity = new ShipmentJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setOrderId(id(domain.getOrder().getId()));
        entity.setDeliveryAddress(toEmbeddable(domain.getDeliveryAddress()));
        entity.setStatus(domain.getStatus());
        return entity;
    }

    public static Shipment toDomain(ShipmentJpaEntity entity, Order order) {
        return Shipment.restore(uuid(entity.getId()), order,
                toAddress(entity.getDeliveryAddress()), entity.getStatus());
    }
}