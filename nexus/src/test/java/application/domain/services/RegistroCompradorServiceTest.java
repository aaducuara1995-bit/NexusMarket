package application.domain.services;

import application.domain.entities.Comprador;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoComercial;
import application.domain.enums.RolUsuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistroCompradorServiceTest {

    private final RepositoriosEnMemoria.Compradores repo = new RepositoriosEnMemoria.Compradores();
    private final RegistroCompradorService servicio = new RegistroCompradorService(repo);

    @Test
    void creaElPerfilDeCompradorHabilitado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);

        Comprador comprador = servicio.registrar(usuario, Fixtures.direccion());

        assertEquals(usuario.getId(), comprador.getUsuarioId());
        assertEquals(EstadoComercial.Habilitado, comprador.getEstadoComercial());
        assertEquals(1, repo.guardados.size());
    }

    @Test
    void unUsuarioNoTieneDosPerfilesDeComprador() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);
        servicio.registrar(usuario, Fixtures.direccion());

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(usuario, Fixtures.direccion()));
        assertEquals(1, repo.guardados.size());
    }

    @Test
    void rechazaUnUsuarioConOtroRol() {
        Usuario vendedor = Fixtures.usuario(RolUsuario.Vendedor);

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(vendedor, Fixtures.direccion()));
    }

    @Test
    void rechazaUnUsuarioBloqueado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);
        usuario.bloquear();

        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(usuario, Fixtures.direccion()));
    }

    @Test
    void exigeUnaDireccionPrincipal() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Comprador);

        assertThrows(IllegalArgumentException.class, () -> servicio.registrar(usuario, null));
        assertEquals(0, repo.guardados.size());
    }
}