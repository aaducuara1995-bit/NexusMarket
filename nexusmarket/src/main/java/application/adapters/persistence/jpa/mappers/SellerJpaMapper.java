package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.SellerJpaEntity;
import application.domain.entities.Seller;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper between the Seller domain entity and its JPA entity. */
public final class SellerJpaMapper {

    private SellerJpaMapper() {
    }

    public static SellerJpaEntity toEntity(Seller domain) {
        SellerJpaEntity entity = new SellerJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setUserId(id(domain.getUserId()));
        return entity;
    }

    public static Seller toDomain(SellerJpaEntity entity) {
        return Seller.restore(uuid(entity.getId()), uuid(entity.getUserId()));
    }
}