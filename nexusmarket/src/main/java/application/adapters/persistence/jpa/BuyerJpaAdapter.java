package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.BuyerJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaBuyerRepository;
import application.domain.entities.Buyer;
import application.domain.ports.out.BuyerRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Output persistence adapter for Buyer backed by Spring Data JPA (MySQL).  */
@Repository
public class BuyerJpaAdapter implements BuyerRepositoryPort {

    private final SpringDataJpaBuyerRepository repository;

    public BuyerJpaAdapter(SpringDataJpaBuyerRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return repository.existsByUserId(userId.toString());
    }

    @Override
    public Optional<Buyer> findById(UUID id) {
        return repository.findById(id.toString()).map(BuyerJpaMapper::toDomain);
    }

    @Override
    public Optional<Buyer> findByUserId(UUID userId) {
        return repository.findByUserId(userId.toString()).map(BuyerJpaMapper::toDomain);
    }

    @Override
    public void save(Buyer buyer) {
        repository.save(BuyerJpaMapper.toEntity(buyer));
    }
}