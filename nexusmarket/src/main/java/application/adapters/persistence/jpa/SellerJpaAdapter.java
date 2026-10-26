package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.SellerJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaSellerRepository;
import application.domain.entities.Seller;
import application.domain.ports.out.SellerRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Output persistence adapter for Seller backed by Spring Data JPA (MySQL).  */
@Repository
public class SellerJpaAdapter implements SellerRepositoryPort {

    private final SpringDataJpaSellerRepository repository;

    public SellerJpaAdapter(SpringDataJpaSellerRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return repository.existsByUserId(userId.toString());
    }

    @Override
    public Optional<Seller> findById(UUID id) {
        return repository.findById(id.toString()).map(SellerJpaMapper::toDomain);
    }

    @Override
    public Optional<Seller> findByUserId(UUID userId) {
        return repository.findByUserId(userId.toString()).map(SellerJpaMapper::toDomain);
    }

    @Override
    public void save(Seller seller) {
        repository.save(SellerJpaMapper.toEntity(seller));
    }
}