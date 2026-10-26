package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.domain.entities.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output persistence adapter for Order backed by Spring Data JPA (MySQL).
 * The buyer is rebuilt through JpaReferenceResolver.
 */
@Repository
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final SpringDataJpaOrderRepository repository;
    private final JpaReferenceResolver resolver;

    public OrderJpaAdapter(SpringDataJpaOrderRepository repository,
                           JpaReferenceResolver resolver) {
        this.repository = repository;
        this.resolver = resolver;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return repository.findById(id.toString()).map(resolver::order);
    }

    @Override
    public List<Order> findByBuyer(UUID buyerId) {
        return repository.findByBuyerId(buyerId.toString()).stream().map(resolver::order).toList();
    }

    @Override
    public List<Order> findAll() {
        return repository.findAll().stream().map(resolver::order).toList();
    }

    @Override
    public void save(Order order) {
        repository.save(OrderJpaMapper.toEntity(order));
    }
}