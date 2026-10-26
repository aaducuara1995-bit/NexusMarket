package application.domain.valueobjects;

public record Address(String street, String city, String department, String postalCode) {

    public Address {
        street = required(street, "Street");
        city = required(city, "City");
        department = required(department, "Department");
        postalCode = required(postalCode, "Postal code");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " cannot be empty.");
        return value.trim();
    }
}