package application.domain.entities;

import application.domain.enums.CommercialStatus;
import application.domain.valueobjects.Address;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Buyer {

    private final UUID id;
    private final UUID userId;
    private Address mainAddress;
    private final List<Address> additionalAddresses = new ArrayList<>();
    private CommercialStatus commercialStatus;

    public Buyer(UUID userId, Address mainAddress) {
        if (userId == null)
            throw new IllegalArgumentException("The buyer must be linked to a user.");
        if (mainAddress == null)
            throw new IllegalArgumentException("The buyer must have a main address.");

        this.id = UUID.randomUUID();
        this.userId = userId;
        this.mainAddress = mainAddress;
        this.commercialStatus = CommercialStatus.ENABLED;
    }

    private Buyer(UUID id, UUID userId, Address mainAddress,
                  List<Address> additionalAddresses, CommercialStatus commercialStatus) {
        this.id = id;
        this.userId = userId;
        this.mainAddress = mainAddress;
        this.additionalAddresses.addAll(additionalAddresses);
        this.commercialStatus = commercialStatus;
    }

    /** Rebuilds a buyer saved by a persistence adapter. It is not for creating new ones. */
    public static Buyer restore(UUID id, UUID userId, Address mainAddress,
                                List<Address> additionalAddresses, CommercialStatus commercialStatus) {
        Restore.require(id, "id");
        Restore.require(userId, "userId");
        Restore.require(mainAddress, "mainAddress");
        Restore.require(additionalAddresses, "additionalAddresses");
        Restore.require(commercialStatus, "commercialStatus");
        return new Buyer(id, userId, mainAddress, additionalAddresses, commercialStatus);
    }

    public void addAdditionalAddress(Address address) {
        if (address == null)
            throw new IllegalArgumentException("Address is required.");
        if (hasAddress(address))
            throw new IllegalStateException("That address is already registered.");

        additionalAddresses.add(address);
    }

    public void removeAdditionalAddress(Address address) {
        if (!additionalAddresses.remove(address))
            throw new IllegalArgumentException("That address is not among the additional ones.");
    }

    public void changeMainAddress(Address newAddress) {
        if (newAddress == null)
            throw new IllegalArgumentException("Address is required.");
        if (newAddress.equals(mainAddress))
            throw new IllegalStateException("That is already the main address.");

        additionalAddresses.remove(newAddress);
        additionalAddresses.add(mainAddress);
        mainAddress = newAddress;
    }

    public boolean hasAddress(Address address) {
        return mainAddress.equals(address)
                || additionalAddresses.contains(address);
    }

    public void restrict() {
        if (commercialStatus == CommercialStatus.RESTRICTED)
            throw new IllegalStateException("The buyer is already restricted.");
        commercialStatus = CommercialStatus.RESTRICTED;
    }

    public void enable() {
        if (commercialStatus == CommercialStatus.ENABLED)
            throw new IllegalStateException("The buyer is already enabled.");
        commercialStatus = CommercialStatus.ENABLED;
    }

    public boolean canPurchase() {
        return commercialStatus == CommercialStatus.ENABLED;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public Address getMainAddress() { return mainAddress; }
    public CommercialStatus getCommercialStatus() { return commercialStatus; }
    public List<Address> getAdditionalAddresses() {
        return Collections.unmodifiableList(additionalAddresses);
    }
}