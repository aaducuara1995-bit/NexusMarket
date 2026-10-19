package application.domain.services;

import application.domain.entities.Devolucion;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoDevolucion;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResolucionDevolucionServiceTest {

    private final ResolucionDevolucionService servicio =
            new ResolucionDevolucionService(new AutorizacionService());

    private final Usuario admin = Fixtures.usuario(RolUsuario.Administrador);

    private Devolucion devolucionSolicitada() {
        return new Devolucion(Fixtures.pedidoEntregado(Fixtures.producto(TipoProducto.Digital), 1),
                "Llego defectuoso");
    }

    @Test
    void elAdministradorApruebaLaDevolucion() {
        Devolucion devolucion = devolucionSolicitada();

        servicio.aprobar(admin, devolucion);

        assertEquals(EstadoDevolucion.Aprobada, devolucion.getEstado());
    }

    @Test
    void elAdministradorRechazaLaDevolucion() {
        Devolucion devolucion = devolucionSolicitada();

        servicio.rechazar(admin, devolucion);

        assertEquals(EstadoDevolucion.Rechazada, devolucion.getEstado());
    }

    @Test
    void elCompradorNoPuedeResolverSuPropiaDevolucion() {
        Devolucion devolucion = devolucionSolicitada();
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);

        assertThrows(AccesoDenegadoException.class, () -> servicio.aprobar(comprador, devolucion));
        assertEquals(EstadoDevolucion.Solicitada, devolucion.getEstado());
    }

    @Test
    void elOperadorLogisticoNoPuedeResolverDevoluciones() {
        Devolucion devolucion = devolucionSolicitada();
        Usuario operador = Fixtures.usuario(RolUsuario.OperadorLogistico);

        assertThrows(AccesoDenegadoException.class, () -> servicio.rechazar(operador, devolucion));
    }

    @Test
    void unaDevolucionSoloSeResuelveUnaVez() {
        Devolucion devolucion = devolucionSolicitada();
        servicio.aprobar(admin, devolucion);

        assertThrows(IllegalStateException.class, () -> servicio.rechazar(admin, devolucion));
        assertEquals(EstadoDevolucion.Aprobada, devolucion.getEstado());
    }
}