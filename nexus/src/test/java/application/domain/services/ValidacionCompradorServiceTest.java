package application.domain.services;

import application.domain.entities.Comprador;
import application.domain.entities.Usuario;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.Direccion;
import application.domain.valueobjects.DocumentoIdentidad;
import application.domain.valueobjects.NombreCompleto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionCompradorServiceTest {

    private final ValidacionCompradorService servicio = new ValidacionCompradorService();

    private Usuario usuario(RolUsuario rol) {
        return new Usuario(
                new NombreCompleto("Ana", "Perez"),
                new CorreoElectronico("ana@correo.com"),
                new DocumentoIdentidad("123456"),
                rol);
    }

    private Comprador comprador(Usuario usuario) {
        return new Comprador(usuario.getId(),
                new Direccion("Calle 1 # 2-3", "Medellin", "Antioquia", "050001"));
    }

    @Test
    void unCompradorHabilitadoPuedeComprar() {
        Usuario usuario = usuario(RolUsuario.Comprador);
        Comprador comprador = comprador(usuario);

        assertTrue(servicio.puedeComprar(usuario, comprador));
        assertDoesNotThrow(() -> servicio.exigirHabilitado(usuario, comprador));
    }

    @Test
    void unCompradorRestringidoNoPuedeComprar() {
        Usuario usuario = usuario(RolUsuario.Comprador);
        Comprador comprador = comprador(usuario);
        comprador.restringir();

        assertFalse(servicio.puedeComprar(usuario, comprador));
        assertThrows(IllegalStateException.class, () -> servicio.exigirHabilitado(usuario, comprador));
    }

    @Test
    void unUsuarioBloqueadoNoPuedeComprar() {
        Usuario usuario = usuario(RolUsuario.Comprador);
        Comprador comprador = comprador(usuario);
        usuario.bloquear();

        assertFalse(servicio.puedeComprar(usuario, comprador));
        assertThrows(IllegalStateException.class, () -> servicio.exigirHabilitado(usuario, comprador));
    }

    @Test
    void unUsuarioConOtroRolNoPuedeComprar() {
        Usuario vendedor = usuario(RolUsuario.Vendedor);
        Comprador comprador = comprador(vendedor);

        assertFalse(servicio.puedeComprar(vendedor, comprador));
        assertThrows(AccesoDenegadoException.class, () -> servicio.exigirHabilitado(vendedor, comprador));
    }

    @Test
    void elCompradorDebeCorresponderAlUsuario() {
        Usuario usuario = usuario(RolUsuario.Comprador);
        Comprador deOtraPersona = comprador(usuario(RolUsuario.Comprador));

        assertFalse(servicio.puedeComprar(usuario, deOtraPersona));
        assertThrows(AccesoDenegadoException.class, () -> servicio.exigirHabilitado(usuario, deOtraPersona));
    }
}