package application.domain.valueobjects;

public record FullName(String firstNames, String lastNames) {

    public FullName {
        if (firstNames == null || firstNames.isBlank())
            throw new IllegalArgumentException("First names cannot be empty.");
        if (lastNames == null || lastNames.isBlank())
            throw new IllegalArgumentException("Last names cannot be empty.");

        firstNames = firstNames.trim();
        lastNames = lastNames.trim();
    }
}