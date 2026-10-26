package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import application.domain.entities.Invoice;
import application.domain.entities.Order;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.money;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper for Invoice. The order is supplied when reading. */
public final class InvoiceJpaMapper {

    private InvoiceJpaMapper() {
    }

    public static InvoiceJpaEntity toEntity(Invoice domain) {
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setOrderId(id(domain.getOrder().getId()));
        entity.setTotalAmount(domain.getTotal().amount());
        entity.setTotalCurrency(domain.getTotal().currency());
        entity.setDate(domain.getDate());
        return entity;
    }

    public static Invoice toDomain(InvoiceJpaEntity entity, Order order) {
        return Invoice.restore(uuid(entity.getId()), order,
                money(entity.getTotalAmount(), entity.getTotalCurrency()), entity.getDate());
    }
}