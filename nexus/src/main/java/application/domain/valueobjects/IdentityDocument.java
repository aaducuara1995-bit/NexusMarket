package application.domain.valueobjects;

public record IdentityDocument(String value) {

    public IdentityDocument {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Identity document cannot be empty.");

        value = value.trim();

        if (!value.chars().allMatch(Character::isDigit))
            throw new IllegalArgumentException("Identity document must contain only digits.");
    }
}