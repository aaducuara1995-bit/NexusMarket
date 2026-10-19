package application.domain.services;

import application.domain.entities.Usuario;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.DocumentoIdentidad;
import application.domain.valueobjects.NombreCompleto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutorizacionServiceTest {

    private final AutorizacionService servicio = new AutorizacionService();

    private Usuario usuario(RolUsuario rol) {
        return new Usuario(
                new NombreCompleto("Ana", "Perez"),
                new CorreoElectronico("ana@correo.com"),
                new DocumentoIdentidad("123456"),
                rol);
    }

    @Test
    void soloElAdministradorRegistraVendedores() {
        assertTrue(servicio.puedeEjecutar(usuario(RolUsuario.Administrador), ProcesoNegocio.RegistroVendedores));
        assertFalse(servicio.puedeEjecutar(usuario(RolUsuario.Vendedor), ProcesoNegocio.RegistroVendedores));
        assertFalse(servicio.puedeEjecutar(usuario(RolUsuario.Comprador), ProcesoNegocio.RegistroVendedores));
    }

    @Test
    void soloElVendedorRegistraProductos() {
        assertTrue(servicio.puedeEjecutar(usuario(RolUsuario.Vendedor), ProcesoNegocio.RegistroProductos));
        assertFalse(servicio.puedeEjecutar(usuario(RolUsuario.Administrador), ProcesoNegocio.RegistroProductos));
        assertFalse(servicio.puedeEjecutar(usuario(RolUsuario.OperadorLogistico), ProcesoNegocio.RegistroProductos));
    }

    @Test
    void elInventarioLoAdministranVendedorYOperadorLogistico() {
        assertTrue(servicio.puedeEjecutar(usuario(RolUsuario.Vendedor), ProcesoNegocio.AdministracionInventario));
        assertTrue(servicio.puedeEjecutar(usuario(RolUsuario.OperadorLogistico), ProcesoNegocio.AdministracionInventario));
        assertFalse(servicio.puedeEjecutar(usuario(RolUsuario.Comprador), ProcesoNegocio.AdministracionInventario));
    }

    @Test
    void losReembolsosLosGestionanCompradorYAdministrador() {
        assertTrue(servicio.puedeEjecutar(usuario(RolUsuario.Comprador), ProcesoNegocio.GestionReembolsos));
        assertTrue(servicio.puedeEjecutar(usuario(RolUsuario.Administrador), ProcesoNegocio.GestionReembolsos));
        assertFalse(servicio.puedeEjecutar(usuario(RolUsuario.Vendedor), ProcesoNegocio.GestionReembolsos));
    }

    @Test
    void elSupervisorNoEjecutaNingunProceso() {
        Usuario supervisor = usuario(RolUsuario.Supervisor);
        for (ProcesoNegocio proceso : ProcesoNegocio.values())
            assertFalse(servicio.puedeEjecutar(supervisor, proceso));
    }

    @Test
    void exigirPermisoLanzaExcepcionSiElRolNoCorresponde() {
        assertThrows(AccesoDenegadoException.class,
                () -> servicio.exigirPermiso(usuario(RolUsuario.Comprador), ProcesoNegocio.RegistroProductos));
    }

    @Test
    void exigirPermisoNoLanzaNadaSiElRolCorresponde() {
        assertDoesNotThrow(
                () -> servicio.exigirPermiso(usuario(RolUsuario.Vendedor), ProcesoNegocio.RegistroProductos));
    }

    @Test
    void unUsuarioBloqueadoNoPuedeEjecutarNada() {
        Usuario bloqueado = usuario(RolUsuario.Comprador);
        bloqueado.bloquear();

        assertFalse(servicio.puedeEjecutar(bloqueado, ProcesoNegocio.GestionPedidos));
        assertThrows(AccesoDenegadoException.class,
                () -> servicio.exigirPermiso(bloqueado, ProcesoNegocio.GestionPedidos));
    }
}