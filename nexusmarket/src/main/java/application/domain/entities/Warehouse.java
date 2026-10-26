package application.domain.entities;

import application.domain.enums.WarehouseOwnership;
import application.domain.enums.WarehouseType;
import application.domain.valueobjects.Address;

import java.util.UUID;

public class Warehouse {

    private final UUID id;
    private final String name;
    private final WarehouseType type;
    private final Address address;
    private final WarehouseOwnership ownership;
    private final UUID sellerId;

    private Warehouse(UUID id, String name, WarehouseType type, Address address,
                   WarehouseOwnership ownership, UUID sellerId) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Warehouse name cannot be empty.");
        if (type == null)
            throw new IllegalArgumentException("Warehouse type is required.");
        if (type == WarehouseType.PHYSICAL && address == null)
            throw new IllegalArgumentException("A physical warehouse must have an address.");
        if (type == WarehouseType.DIGITAL && address != null)
            throw new IllegalArgumentException("A digital warehouse has no physical address.");

        this.id = id;
        this.name = name.trim();
        this.type = type;
        this.address = address;
        this.ownership = ownership;
        this.sellerId = sellerId;
    }

    public static Warehouse ofMarketplace(String name, WarehouseType type, Address address) {
        return new Warehouse(UUID.randomUUID(), name, type, address, WarehouseOwnership.MARKETPLACE, null);
    }

    public static Warehouse ofSeller(UUID sellerId, String name,
                                    WarehouseType type, Address address) {
        if (sellerId == null)
            throw new IllegalArgumentException("A seller warehouse must have an owner.");
        return new Warehouse(UUID.randomUUID(), name, type, address, WarehouseOwnership.SELLER, sellerId);
    }

    /** Rebuilds a warehouse saved by a persistence adapter. It is not for creating new ones. */
    public static Warehouse restore(UUID id, String name, WarehouseType type, Address address,
                                    WarehouseOwnership ownership, UUID sellerId) {
        Restore.require(id, "id");
        Restore.require(ownership, "ownership");
        if (ownership == WarehouseOwnership.SELLER)
            Restore.require(sellerId, "sellerId");
        return new Warehouse(id, name, type, address, ownership, sellerId);
    }

    public boolean isDigital() {
        return type == WarehouseType.DIGITAL;
    }

    public boolean isMarketplaceOwned() {
        return ownership == WarehouseOwnership.MARKETPLACE;
    }

    public boolean belongsTo(UUID sellerIdToCheck) {
        return ownership == WarehouseOwnership.SELLER
                && sellerId.equals(sellerIdToCheck);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public WarehouseType getType() { return type; }
    public Address getAddress() { return address; }
    public WarehouseOwnership getOwnership() { return ownership; }
    public UUID getSellerId() { return sellerId; }
}