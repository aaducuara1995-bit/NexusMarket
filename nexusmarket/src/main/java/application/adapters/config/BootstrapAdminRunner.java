package application.adapters.config;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator when the platform starts with no admin at all.
 * Every other user is registered through the use cases, and only an admin can register
 * staff roles, so someone has to exist first. Its data can be changed with the
 * nexusmarket.bootstrap-admin.* properties.
 */
@Component
public class BootstrapAdminRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final UserRepositoryPort users;
    private final String email;
    private final String document;

    public BootstrapAdminRunner(UserRepositoryPort users,
                                @Value("${nexusmarket.bootstrap-admin.email:admin@nexusmarket.com}") String email,
                                @Value("${nexusmarket.bootstrap-admin.document:1000000000}") String document) {
        this.users = users;
        this.email = email;
        this.document = document;
    }

    @Override
    public void run(String... args) {
        boolean adminExists = users.findAll().stream().anyMatch(user -> user.hasRole(UserRole.ADMIN));
        if (adminExists) {
            log.info("NexusMarket: an administrator already exists, nothing to create.");
            return;
        }
        User admin = new User(new FullName("Administrador", "NexusMarket"), new Email(email),
                new IdentityDocument(document), UserRole.ADMIN);
        users.save(admin);
        log.info("NexusMarket: initial administrator created with email {}.", email);
    }
}