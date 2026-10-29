package application.adapters.config;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import application.domain.services.InMemoryRepositories;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.FullName;
import application.domain.valueobjects.IdentityDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BootstrapAdminRunnerTest {

    private final InMemoryRepositories.Users users = new InMemoryRepositories.Users();
    private final BootstrapAdminRunner runner = new BootstrapAdminRunner(users,
            new InMemoryRepositories.FakePasswords(), "admin@nexusmarket.com", "1000000000", "Admin12345*");

    @Test
    void anEmptyPlatformGetsAnAdminThatCanSignIn() {
        runner.run();

        User admin = users.findByEmail(new Email("admin@nexusmarket.com")).orElseThrow();
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertEquals("hash:Admin12345*", admin.getPasswordHash());
    }

    @Test
    void anAdminCreatedBeforePasswordsExistedGetsOne() {
        User oldAdmin = new User(new FullName("Administrador", "NexusMarket"), new Email("admin@nexusmarket.com"),
                new IdentityDocument("1000000000"), UserRole.ADMIN);
        users.save(oldAdmin);

        runner.run();

        assertEquals(1, users.saved.size());
        assertTrue(users.findById(oldAdmin.getId()).orElseThrow().hasPassword());
    }

    @Test
    void anAdminWithPasswordIsLeftAsItIs() {
        runner.run();
        String firstHash = users.saved.get(0).getPasswordHash();

        new BootstrapAdminRunner(users, new InMemoryRepositories.FakePasswords(),
                "admin@nexusmarket.com", "1000000000", "Other-password").run();

        assertEquals(firstHash, users.saved.get(0).getPasswordHash());
        assertEquals(1, users.saved.size());
    }
}