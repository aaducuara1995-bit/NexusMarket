package application.domain.services;

import application.domain.entities.Usuario;
import application.domain.enums.ClasificacionBodega;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoBodega;
import application.domain.exceptions.AccesoDenegadoException;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.Direccion;
import application.domain.valueobjects.DocumentoIdentidad;
import application.domain.valueobjects.NombreCompleto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncorporacionVendedorServiceTest {

    private final IncorporacionVendedorService servicio =
            new IncorporacionVendedorService(new AutorizacionService());

    private Usuario usuario(RolUsuario rol) {
        return new Usuario(
                new NombreCompleto("Ana", "Perez"),
                new CorreoElectronico("ana@correo.com"),
                new DocumentoIdentidad("123456"),
                rol);
    }

    private Direccion direccion() {
        return new Direccion("Calle 1 # 2-3", "Medellin", "Antioquia", "050001");
    }

    @Test
    void elAdministradorIncorporaUnVendedorConSuPrimeraBodega() {
        Usuario admin = usuario(RolUsuario.Administrador);
        Usuario usuarioVendedor = usuario(RolUsuario.Vendedor);

        var resultado = servicio.incorporar(admin, usuarioVendedor,
                "Bodega principal", TipoBodega.Fisica, direccion());

        assertEquals(usuarioVendedor.getId(), resultado.vendedor().getUsuarioId());
        assertTrue(resultado.bodega().perteneceA(resultado.vendedor().getId()));
        assertEquals(ClasificacionBodega.Vendedor, resultado.bodega().getClasificacion());
    }

    @Test
    void unNoAdministradorNoPuedeIncorporarVendedores() {
        Usuario noAdmin = usuario(RolUsuario.Vendedor);
        Usuario usuarioVendedor = usuario(RolUsuario.Vendedor);

        assertThrows(AccesoDenegadoException.class,
                () -> servicio.incorporar(noAdmin, usuarioVendedor,
                        "Bodega principal", TipoBodega.Fisica, direccion()));
    }

    @Test
    void soloSeIncorporaAUsuariosConRolDeVendedor() {
        Usuario admin = usuario(RolUsuario.Administrador);
        Usuario comprador = usuario(RolUsuario.Comprador);

        assertThrows(IllegalStateException.class,
                () -> servicio.incorporar(admin, comprador,
                        "Bodega principal", TipoBodega.Fisica, direccion()));
    }

    @Test
    void noSeIncorporaAUnUsuarioBloqueado() {
        Usuario admin = usuario(RolUsuario.Administrador);
        Usuario usuarioVendedor = usuario(RolUsuario.Vendedor);
        usuarioVendedor.bloquear();

        assertThrows(IllegalStateException.class,
                () -> servicio.incorporar(admin, usuarioVendedor,
                        "Bodega principal", TipoBodega.Fisica, direccion()));
    }

    @Test
    void unaBodegaFisicaSinDireccionSeRechaza() {
        Usuario admin = usuario(RolUsuario.Administrador);
        Usuario usuarioVendedor = usuario(RolUsuario.Vendedor);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.incorporar(admin, usuarioVendedor,
                        "Bodega principal", TipoBodega.Fisica, null));
    }
}