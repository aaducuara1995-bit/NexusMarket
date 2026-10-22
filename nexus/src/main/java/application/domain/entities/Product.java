package application.domain.entities;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.valueobjects.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final UUID sellerId;
    private final String name;
    private final String description;
    private final ProductType type;
    private final List<String> variants = new ArrayList<>();
    private Money price;
    private ProductStatus status;

    public Product(UUID sellerId, String name, String description,
                    ProductType type, Money price) {
        if (sellerId == null)
            throw new IllegalArgumentException("The product must belong to a seller.");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be empty.");
        if (type == null)
            throw new IllegalArgumentException("Product type is required.");
        validatePrice(price);

        this.id = UUID.randomUUID();
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.status = ProductStatus.SUSPENDED;
    }

    public void addVariant(String variant) {
        if (variant == null || variant.isBlank())
            throw new IllegalArgumentException("Variant cannot be empty.");
        if (variants.contains(variant))
            throw new IllegalStateException("That variant already exists in the product.");

        variants.add(variant);
    }

    public void changePrice(Money newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }

    public void publish() {
        if (status == ProductStatus.DISCONTINUED)
            throw new IllegalStateException("A discontinued product cannot be published again.");
        if (status == ProductStatus.PUBLISHED)
            throw new IllegalStateException("The product is already published.");

        status = ProductStatus.PUBLISHED;
    }

    public void suspend() {
        if (status == ProductStatus.DISCONTINUED)
            throw new IllegalStateException("A discontinued product cannot be suspended.");
        if (status == ProductStatus.SUSPENDED)
            throw new IllegalStateException("The product is already suspended.");

        status = ProductStatus.SUSPENDED;
    }

    public void discontinue() {
        if (status == ProductStatus.DISCONTINUED)
            throw new IllegalStateException("The product is already discontinued.");

        status = ProductStatus.DISCONTINUED;
    }

    public boolean isPublished() {
        return status == ProductStatus.PUBLISHED;
    }

    public boolean isPhysical() {
        return type == ProductType.PHYSICAL;
    }

    public boolean belongsTo(UUID sellerIdToCheck) {
        return sellerId.equals(sellerIdToCheck);
    }

    private static void validatePrice(Money price) {
        if (price == null || price.amount().signum() <= 0)
            throw new IllegalArgumentException("Price must be greater than zero.");
    }

    public UUID getId() { return id; }
    public UUID getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProductType getType() { return type; }
    public Money getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public List<String> getVariants() { return Collections.unmodifiableList(variants); }
}