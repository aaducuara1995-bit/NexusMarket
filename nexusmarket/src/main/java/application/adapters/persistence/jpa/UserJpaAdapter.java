package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.entities.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Output persistence adapter for User backed by Spring Data JPA (MySQL).  */
@Repository
public class UserJpaAdapter implements UserRepositoryPort {

    private final SpringDataJpaUserRepository repository;

    public UserJpaAdapter(SpringDataJpaUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByDocument(IdentityDocument document) {
        return repository.existsByDocument(document.value());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id.toString()).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return repository.findByEmail(email.value()).map(UserJpaMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(UserJpaMapper::toDomain).toList();
    }

    @Override
    public void save(User user) {
        repository.save(UserJpaMapper.toEntity(user));
    }
}