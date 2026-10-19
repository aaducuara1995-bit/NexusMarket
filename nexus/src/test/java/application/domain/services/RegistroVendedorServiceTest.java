package application.domain.services;

import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistroVendedorServiceTest {

    private final RepositoriosEnMemoria.Vendedores repo = new RepositoriosEnMemoria.Vendedores();
    private final RegistroVendedorService servicio =
            new RegistroVendedorService(new AutorizacionService(), repo);

    private final Usuario admin = Fixtures.usuario(RolUsuario.Administrador);

    @Test
    void elAdministradorRegistraElPerfilDeVendedor() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);

        Vendedor vendedor = servicio.registrar(admin, usuario);

        assertEquals(usuario.getId(), vendedor.getUsuarioId());
        assertEquals(1, repo.guardados.size());
    }

    @Test
    void unUsuarioNoTieneDosPerfilesDeVendedor() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        servicio.registrar(admin, usuario);

        assertThrows(IllegalStateException.class, () -> servicio.registrar(admin, usuario));
        assertEquals(1, repo.guardados.size());
    }

    @Test
    void soloUnAdministradorRegistraVendedores() {
        Usuario otroVendedor = Fixtures.usuario(RolUsuario.Vendedor);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.registrar(otroVendedor, Fixtures.usuario(RolUsuario.Vendedor)));
    }

    @Test
    void rechazaUnUsuarioConOtroRol() {
        assertThrows(IllegalStateException.class,
                () -> servicio.registrar(admin, Fixtures.usuario(RolUsuario.Comprador)));
    }

    @Test
    void rechazaUnUsuarioBloqueado() {
        Usuario usuario = Fixtures.usuario(RolUsuario.Vendedor);
        usuario.bloquear();

        assertThrows(IllegalStateException.class, () -> servicio.registrar(admin, usuario));
    }
}