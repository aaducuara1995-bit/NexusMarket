package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.CartJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaCartRepository;
import application.domain.entities.Cart;
import application.domain.ports.out.CartRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Output persistence adapter for Cart backed by Spring Data JPA (MySQL).
 * The buyer and the products of each line are rebuilt through JpaReferenceResolver.
 */
@Repository
public class CartJpaAdapter implements CartRepositoryPort {

    private final SpringDataJpaCartRepository repository;
    private final JpaReferenceResolver resolver;

    public CartJpaAdapter(SpringDataJpaCartRepository repository,
                          JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Optional<Cart> findByBuyer(UUID buyerId) {
        return repository.findByBuyerId(buyerId.toString())
                .map(entity -> CartJpaMapper.toDomain(entity, resolver.buyer(entity.getBuyerId()),
                        productId -> resolver.product(productId.toString())));
    }

    @Override
    public void save(Cart cart) {
        repository.save(CartJpaMapper.toEntity(cart));
    }
}