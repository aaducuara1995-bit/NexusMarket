package application.adapters.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity (repository DTO) for Cart, stored in MySQL table carts.
 * Active cart of a buyer. Lines live in cart_items.
 * References to other aggregates are kept as identifiers; the adapter rebuilds them.
 */
@Entity
@Table(name = "carts")
public class CartJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "buyer_id", length = 36, nullable = false, unique = true)
    private String buyerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @OrderColumn(name = "line_number")
    private List<CartItemEmbeddable> items = new ArrayList<>();

    public CartJpaEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public List<CartItemEmbeddable> getItems() {
        return items;
    }

    public void setItems(List<CartItemEmbeddable> items) {
        this.items = items;
    }
}