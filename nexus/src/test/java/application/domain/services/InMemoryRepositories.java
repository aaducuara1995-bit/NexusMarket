package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentityDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Simple implementations of the output ports, for tests only. */
final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    static class Users implements UserRepositoryPort {
        final List<User> saved = new ArrayList<>();

        public boolean existsByEmail(Email email) {
            return saved.stream().anyMatch(u -> u.getEmail().equals(email));
        }

        public boolean existsByDocument(IdentityDocument document) {
            return saved.stream().anyMatch(u -> u.getDocument().equals(document));
        }

        public void save(User user) {
            saved.add(user);
        }
    }

    static class Buyers implements BuyerRepositoryPort {
        final List<Buyer> saved = new ArrayList<>();

        public boolean existsByUserId(UUID userId) {
            return saved.stream().anyMatch(c -> c.getUserId().equals(userId));
        }

        public void save(Buyer buyer) {
            saved.add(buyer);
        }
    }

    static class Sellers implements SellerRepositoryPort {
        final List<Seller> saved = new ArrayList<>();

        public boolean existsByUserId(UUID userId) {
            return saved.stream().anyMatch(v -> v.getUserId().equals(userId));
        }

        public void save(Seller seller) {
            saved.add(seller);
        }
    }

    static class Warehouses implements WarehouseRepositoryPort {
        final List<Warehouse> saved = new ArrayList<>();

        public boolean existsByName(String name) {
            return saved.stream().anyMatch(b -> b.getName().equalsIgnoreCase(name));
        }

        public void save(Warehouse warehouse) {
            saved.add(warehouse);
        }
    }

    static class Products implements ProductRepositoryPort {
        final List<Product> saved = new ArrayList<>();

        public boolean existsNameForSeller(UUID sellerId, String name) {
            return saved.stream().anyMatch(p ->
                    p.getSellerId().equals(sellerId) && p.getName().equalsIgnoreCase(name));
        }

        public void save(Product product) {
            saved.add(product);
        }
    }
}