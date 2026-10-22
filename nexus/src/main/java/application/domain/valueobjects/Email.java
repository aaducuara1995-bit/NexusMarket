package application.domain.valueobjects;

public record Email(String value) {

    public Email {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Email cannot be empty.");

        value = value.trim().toLowerCase();

        if (!value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Email format is not valid.");
    }
}