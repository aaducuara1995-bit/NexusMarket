package application.domain.valueobjects;

public record Direccion(String calle, String ciudad, String departamento, String codigoPostal) {

    public Direccion {
        calle = requerido(calle, "La calle");
        ciudad = requerido(ciudad, "La ciudad");
        departamento = requerido(departamento, "El departamento");
        codigoPostal = requerido(codigoPostal, "El código postal");
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank())
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        return valor.trim();
    }
}