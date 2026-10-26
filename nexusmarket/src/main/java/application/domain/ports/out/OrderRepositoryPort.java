package application.domain.ports.out;

import application.domain.entities.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {

    Optional<Order> findById(UUID id);

    List<Order> findByBuyer(UUID buyerId);

    List<Order> findAll();

    void save(Order order);
}