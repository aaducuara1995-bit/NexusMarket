package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.OrderItemEmbeddable;
import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.domain.entities.Buyer;
import application.domain.entities.Order;
import application.domain.entities.OrderItem;

import java.util.ArrayList;
import java.util.List;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.money;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.toAddress;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.toEmbeddable;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper for Order. Lines keep the product data copied at purchase time; the buyer is supplied. */
public final class OrderJpaMapper {

    private OrderJpaMapper() {
    }

    public static OrderJpaEntity toEntity(Order domain) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setBuyerId(id(domain.getBuyer().getId()));
        entity.setDate(domain.getDate());
        entity.setDeliveryAddress(toEmbeddable(domain.getDeliveryAddress()));
        entity.setStatus(domain.getStatus());
        List<OrderItemEmbeddable> items = new ArrayList<>();
        for (OrderItem item : domain.getItems()) {
            OrderItemEmbeddable line = new OrderItemEmbeddable();
            line.setProductId(id(item.getProductId()));
            line.setProductName(item.getProductName());
            line.setType(item.getType());
            line.setQuantity(item.getQuantity());
            line.setUnitPriceAmount(item.getUnitPrice().amount());
            line.setUnitPriceCurrency(item.getUnitPrice().currency());
            items.add(line);
        }
        entity.setItems(items);
        return entity;
    }

    public static Order toDomain(OrderJpaEntity entity, Buyer buyer) {
        List<OrderItem> items = entity.getItems().stream()
                .map(line -> OrderItem.restore(uuid(line.getProductId()), line.getProductName(), line.getType(),
                        line.getQuantity(), money(line.getUnitPriceAmount(), line.getUnitPriceCurrency())))
                .toList();
        return Order.restore(uuid(entity.getId()), buyer, items, entity.getDate(),
                toAddress(entity.getDeliveryAddress()), entity.getStatus());
    }
}