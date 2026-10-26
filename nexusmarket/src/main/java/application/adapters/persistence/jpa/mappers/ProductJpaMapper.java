package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.ProductJpaEntity;
import application.domain.entities.Product;

import java.util.ArrayList;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.money;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper between the Product domain entity and its JPA entity. */
public final class ProductJpaMapper {

    private ProductJpaMapper() {
    }

    public static ProductJpaEntity toEntity(Product domain) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setSellerId(id(domain.getSellerId()));
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setType(domain.getType());
        entity.setPriceAmount(domain.getPrice().amount());
        entity.setPriceCurrency(domain.getPrice().currency());
        entity.setStatus(domain.getStatus());
        entity.setVariants(new ArrayList<>(domain.getVariants()));
        return entity;
    }

    public static Product toDomain(ProductJpaEntity entity) {
        return Product.restore(uuid(entity.getId()), uuid(entity.getSellerId()), entity.getName(),
                entity.getDescription(), entity.getType(),
                money(entity.getPriceAmount(), entity.getPriceCurrency()),
                entity.getStatus(), entity.getVariants());
    }
}