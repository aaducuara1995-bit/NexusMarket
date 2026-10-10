package application.domain.valueobjects;

public record NombreCompleto(String nombres, String apellidos) {

    public NombreCompleto {
        if (nombres == null || nombres.isBlank())
            throw new IllegalArgumentException("Los nombres no pueden estar vacíos.");
        if (apellidos == null || apellidos.isBlank())
            throw new IllegalArgumentException("Los apellidos no pueden estar vacíos.");

        nombres = nombres.trim();
        apellidos = apellidos.trim();
    }
}
