package application.domain.services;

import application.domain.entities.User;
import application.domain.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoriesTest {

    @Test
    void savingTheSameObjectTwiceKeepsASingleCopy() {
        InMemoryRepositories.Users users = new InMemoryRepositories.Users();
        User user = Fixtures.user(UserRole.BUYER);

        users.save(user);
        user.block();
        users.save(user);

        assertEquals(1, users.saved.size());
        assertSame(user, users.findById(user.getId()).orElseThrow());
    }

    @Test
    void findsByEmailAndUserId() {
        InMemoryRepositories.Users users = new InMemoryRepositories.Users();
        InMemoryRepositories.Buyers buyers = new InMemoryRepositories.Buyers();
        User user = Fixtures.user(UserRole.BUYER);
        users.save(user);
        buyers.save(Fixtures.buyer(user));

        assertTrue(users.findByEmail(user.getEmail()).isPresent());
        assertTrue(buyers.findByUserId(user.getId()).isPresent());
    }
}