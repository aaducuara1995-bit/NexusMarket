package application.domain.services;

import application.domain.entities.Usuario;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.DocumentoIdentidad;
import application.domain.valueobjects.NombreCompleto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistroUsuarioServiceTest {

    private final RepositoriosEnMemoria.Usuarios repo = new RepositoriosEnMemoria.Usuarios();
    private final RegistroUsuarioService servicio =
            new RegistroUsuarioService(new AutorizacionService(), repo);

    private final NombreCompleto nombre = new NombreCompleto("Luis", "Gomez");
    private final Usuario admin = Fixtures.usuario(RolUsuario.Administrador);

    private CorreoElectronico correo(String valor) {
        return new CorreoElectronico(valor);
    }

    private DocumentoIdentidad documento(String valor) {
        return new DocumentoIdentidad(valor);
    }

    @Test
    void unCompradorSeRegistraSoloYQuedaGuardado() {
        Usuario usuario = servicio.registrar(null, nombre, correo("luis@correo.com"),
                documento("1001"), RolUsuario.Comprador);

        assertEquals(RolUsuario.Comprador, usuario.getRol());
        assertEquals(1, repo.guardados.size());
    }

    @Test
    void rechazaUnCorreoRepetido() {
        servicio.registrar(null, nombre, correo("luis@correo.com"), documento("1001"), RolUsuario.Comprador);

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(null, nombre, correo("luis@correo.com"),
                        documento("2002"), RolUsuario.Comprador));
        assertEquals(1, repo.guardados.size());
    }

    @Test
    void elCorreoSeComparaSinImportarMayusculas() {
        servicio.registrar(null, nombre, correo("luis@correo.com"), documento("1001"), RolUsuario.Comprador);

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(null, nombre, correo("LUIS@Correo.com"),
                        documento("2002"), RolUsuario.Comprador));
    }

    @Test
    void rechazaUnDocumentoRepetido() {
        servicio.registrar(null, nombre, correo("luis@correo.com"), documento("1001"), RolUsuario.Comprador);

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(null, nombre, correo("otro@correo.com"),
                        documento("1001"), RolUsuario.Comprador));
    }

    @Test
    void unVendedorNoSeAutoRegistra() {
        assertThrows(AccesoDenegadoException.class,
                () -> servicio.registrar(null, nombre, correo("v@correo.com"),
                        documento("3003"), RolUsuario.Vendedor));
    }

    @Test
    void elAdministradorRegistraUnVendedor() {
        Usuario vendedor = servicio.registrar(admin, nombre, correo("v@correo.com"),
                documento("3003"), RolUsuario.Vendedor);

        assertEquals(RolUsuario.Vendedor, vendedor.getRol());
    }

    @Test
    void unCompradorNoPuedeRegistrarVendedores() {
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.registrar(comprador, nombre, correo("v@correo.com"),
                        documento("3003"), RolUsuario.Vendedor));
    }

    @Test
    void soloUnAdministradorRegistraOperadoresLogisticos() {
        assertThrows(AccesoDenegadoException.class,
                () -> servicio.registrar(null, nombre, correo("op@correo.com"),
                        documento("4004"), RolUsuario.OperadorLogistico));

        Usuario operador = servicio.registrar(admin, nombre, correo("op@correo.com"),
                documento("4004"), RolUsuario.OperadorLogistico);
        assertEquals(RolUsuario.OperadorLogistico, operador.getRol());
    }
}