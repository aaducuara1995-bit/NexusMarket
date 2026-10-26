package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.CartItemEmbeddable;
import application.adapters.persistence.jpa.entities.CartJpaEntity;
import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.CartItem;
import application.domain.entities.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper for Cart. The buyer and the products of each line are supplied when reading. */
public final class CartJpaMapper {

    private CartJpaMapper() {
    }

    public static CartJpaEntity toEntity(Cart domain) {
        CartJpaEntity entity = new CartJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setBuyerId(id(domain.getBuyer().getId()));
        List<CartItemEmbeddable> items = new ArrayList<>();
        for (CartItem item : domain.getItems()) {
            CartItemEmbeddable line = new CartItemEmbeddable();
            line.setProductId(id(item.getProduct().getId()));
            line.setQuantity(item.getQuantity());
            items.add(line);
        }
        entity.setItems(items);
        return entity;
    }

    public static Cart toDomain(CartJpaEntity entity, Buyer buyer, Function<UUID, Product> products) {
        List<CartItem> items = entity.getItems().stream()
                .map(line -> CartItem.restore(products.apply(uuid(line.getProductId())), line.getQuantity()))
                .toList();
        return Cart.restore(uuid(entity.getId()), buyer, items);
    }
}