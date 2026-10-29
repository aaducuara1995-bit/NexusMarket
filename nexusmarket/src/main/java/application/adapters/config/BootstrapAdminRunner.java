package application.adapters.config;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.ports.out.PasswordServicePort;
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
 * Makes sure an administrator can sign in when the platform starts. Every other user is
 * registered through the use cases, and only an admin can register staff roles, so
 * someone has to exist first. It creates the admin when there is none, and gives a
 * password to an admin created before passwords existed (phase D). The data comes from
 * the nexusmarket.bootstrap-admin.* properties.
 */
@Component
public class BootstrapAdminRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final UserRepositoryPort users;
    private final PasswordServicePort passwords;
    private final String email;
    private final String document;
    private final String password;

    public BootstrapAdminRunner(UserRepositoryPort users, PasswordServicePort passwords,
                                @Value("${nexusmarket.bootstrap-admin.email:admin@nexusmarket.com}") String email,
                                @Value("${nexusmarket.bootstrap-admin.document:1000000000}") String document,
                                @Value("${nexusmarket.bootstrap-admin.password:Admin12345*}") String password) {
        this.users = users;
        this.passwords = passwords;
        this.email = email;
        this.document = document;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        User admin = users.findAll().stream().filter(user -> user.hasRole(UserRole.ADMIN)).findFirst().orElse(null);
        if (admin == null) {
            admin = new User(new FullName("Administrador", "NexusMarket"), new Email(email),
                    new IdentityDocument(document), UserRole.ADMIN);
            admin.assignPasswordHash(passwords.encrypt(password));
            users.save(admin);
            log.info("NexusMarket: initial administrator created with email {}.", email);
        } else if (!admin.hasPassword()) {
            admin.assignPasswordHash(passwords.encrypt(password));
            users.save(admin);
            log.info("NexusMarket: password assigned to the existing administrator {}.", admin.getEmail().value());
        } else {
            log.info("NexusMarket: an administrator already exists, nothing to create.");
        }
    }
}