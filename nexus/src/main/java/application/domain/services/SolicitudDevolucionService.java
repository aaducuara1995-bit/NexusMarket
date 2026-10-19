package application.domain.services;

import application.domain.entities.Comprador;
import application.domain.entities.Devolucion;
import application.domain.entities.Pedido;
import application.domain.entities.Usuario;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;

import java.util.List;

public class SolicitudDevolucionService {

    private final AutorizacionService autorizacion;

    public SolicitudDevolucionService(AutorizacionService autorizacion) {
        if (autorizacion == null)
            throw new IllegalArgumentException("El servicio de autorizaciÃ³n es obligatorio.");
        this.autorizacion = autorizacion;
    }

    public Devolucion solicitar(Usuario usuario, Comprador comprador, Pedido pedido,
                                String motivo, List<Devolucion> existentes) {
        autorizacion.exigirPermiso(usuario, ProcesoNegocio.GestionDevoluciones);
        if (comprador == null)
            throw new IllegalArgumentException("El comprador es obligatorio.");
        if (pedido == null)
            throw new IllegalArgumentException("El pedido es obligatorio.");
        if (existentes == null)
            throw new IllegalArgumentException("La lista de devoluciones existentes es obligatoria.");

        if (!usuario.tieneRol(RolUsuario.Comprador))
            throw new AccesoDenegadoException("Solo el comprador puede solicitar una devoluciÃ³n.");
        if (!comprador.getUsuarioId().equals(usuario.getId()))
            throw new AccesoDenegadoException("El comprador no corresponde a este usuario.");
        if (!pedido.perteneceA(comprador.getId()))
            throw new AccesoDenegadoException("El pedido pertenece a otro comprador.");

        boolean hayOtraAbierta = existentes.stream()
                .anyMatch(d -> d.getPedido().getId().equals(pedido.getId()) && d.estaAbierta());
        if (hayOtraAbierta)
            throw new IllegalStateException("El pedido ya tiene una devoluciÃ³n en curso o aprobada.");

        // La propia devoluciÃ³n exige que el pedido estÃ© entregado y que haya un motivo.
        return new Devolucion(pedido, motivo);
    }
}