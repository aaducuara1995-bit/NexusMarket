package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.BuyerJpaEntity;
import application.domain.entities.Buyer;

import java.util.ArrayList;

import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.id;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.toAddress;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.toEmbeddable;
import static application.adapters.persistence.jpa.mappers.CommonJpaMapper.uuid;

/** Bidirectional mapper between the Buyer domain entity and its JPA entity. */
public final class BuyerJpaMapper {

    private BuyerJpaMapper() {
    }

    public static BuyerJpaEntity toEntity(Buyer domain) {
        BuyerJpaEntity entity = new BuyerJpaEntity();
        entity.setId(id(domain.getId()));
        entity.setUserId(id(domain.getUserId()));
        entity.setMainAddress(toEmbeddable(domain.getMainAddress()));
        entity.setAdditionalAddresses(new ArrayList<>(domain.getAdditionalAddresses().stream()
                .map(CommonJpaMapper::toEmbeddable).toList()));
        entity.setCommercialStatus(domain.getCommercialStatus());
        return entity;
    }

    public static Buyer toDomain(BuyerJpaEntity entity) {
        return Buyer.restore(uuid(entity.getId()), uuid(entity.getUserId()),
                toAddress(entity.getMainAddress()),
                entity.getAdditionalAddresses().stream().map(CommonJpaMapper::toAddress).toList(),
                entity.getCommercialStatus());
    }
}