package application.domain.valueobjects;

public record DocumentoIdentidad(String valor) {

    public DocumentoIdentidad {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("El documento de identidad no puede estar vacío.");

        valor = valor.trim();

        if (!valor.chars().allMatch(Character::isDigit))
            throw new IllegalArgumentException("El documento de identidad solo debe contener números.");
    }
}