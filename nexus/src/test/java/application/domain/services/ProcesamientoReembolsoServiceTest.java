package application.domain.services;

import application.domain.entities.Pedido;
import application.domain.entities.Reembolso;
import application.domain.entities.Usuario;
import application.domain.enums.EstadoReembolso;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoProducto;
import application.domain.exceptions.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProcesamientoReembolsoServiceTest {

    private final ProcesamientoReembolsoService servicio =
            new ProcesamientoReembolsoService(new AutorizacionService());

    private final Usuario admin = Fixtures.usuario(RolUsuario.Administrador);

    private Reembolso reembolsoPendiente() {
        Pedido pedido = Fixtures.pedidoEntregado(Fixtures.producto(TipoProducto.Digital), 1);
        return new Reembolso(pedido, pedido.calcularTotal());
    }

    @Test
    void elAdministradorProcesaElReembolso() {
        Reembolso reembolso = reembolsoPendiente();

        servicio.procesar(admin, reembolso);

        assertEquals(EstadoReembolso.Procesado, reembolso.getEstado());
    }

    @Test
    void elCompradorNoPuedeProcesarReembolsos() {
        Reembolso reembolso = reembolsoPendiente();
        Usuario comprador = Fixtures.usuario(RolUsuario.Comprador);

        assertThrows(AccesoDenegadoException.class, () -> servicio.procesar(comprador, reembolso));
        assertEquals(EstadoReembolso.Pendiente, reembolso.getEstado());
    }

    @Test
    void unReembolsoNoSeProcesaDosVeces() {
        Reembolso reembolso = reembolsoPendiente();
        servicio.procesar(admin, reembolso);

        assertThrows(IllegalStateException.class, () -> servicio.procesar(admin, reembolso));
    }

    @Test
    void unAdministradorBloqueadoNoPuedeProcesar() {
        Reembolso reembolso = reembolsoPendiente();
        admin.bloquear();

        assertThrows(AccesoDenegadoException.class, () -> servicio.procesar(admin, reembolso));
        assertEquals(EstadoReembolso.Pendiente, reembolso.getEstado());
    }
}