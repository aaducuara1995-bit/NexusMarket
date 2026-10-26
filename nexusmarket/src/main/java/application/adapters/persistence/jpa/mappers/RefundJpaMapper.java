package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.RefundJpaEntity;
import application.domain.entities.Order;
import application.domain.entities.Refund;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.money;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper for Refund. The order is supplied when reading. */
public final class RefundJpaMapper {

    private RefundJpaMapper() {
    }

    public static RefundJpaEntity toEntity(Refund domain) {
        RefundJpaEntity entity = new RefundJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setOrderId(id(domain.getOrder().getId()));
        entity.setAmount(domain.getAmount().amount());
        entity.setCurrency(domain.getAmount().currency());
        entity.setStatus(domain.getStatus());
        return entity;
    }

    public static Refund toDomain(RefundJpaEntity entity, Order order) {
        return Refund.restore(uuid(entity.getId()), order,
                money(entity.getAmount(), entity.getCurrency()), entity.getStatus());
    }
}