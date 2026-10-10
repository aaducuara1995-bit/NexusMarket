package application.domain.valueobjects;

public record CorreoElectronico(String valor) {

    public CorreoElectronico {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException("El correo no puede estar vacío.");

        valor = valor.trim().toLowerCase();

        if (!valor.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("El formato del correo no es válido.");
    }
}