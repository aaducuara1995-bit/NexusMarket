package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.AddressEmbeddable;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.UUID;

/** Conversions shared by all JPA mappers: identifiers, addresses and money. */
public final class CommonJpaMapper {

    private CommonJpaMapper() {
    }

    public static String id(UUID id) {
        return id == null ? null : id.toString();
    }

    public static UUID uuid(String id) {
        return id == null ? null : UUID.fromString(id);
    }

    public static AddressEmbeddable toEmbeddable(Address address) {
        if (address == null)
            return null;
        AddressEmbeddable embeddable = new AddressEmbeddable();
        embeddable.setStreet(address.street());
        embeddable.setCity(address.city());
        embeddable.setDepartment(address.department());
        embeddable.setPostalCode(address.postalCode());
        return embeddable;
    }

    /** An embedded address with every column empty (a digital warehouse) maps to no address. */
    public static Address toAddress(AddressEmbeddable embeddable) {
        if (embeddable == null || embeddable.getStreet() == null)
            return null;
        return new Address(embeddable.getStreet(), embeddable.getCity(),
                embeddable.getDepartment(), embeddable.getPostalCode());
    }

    public static Money money(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }
}