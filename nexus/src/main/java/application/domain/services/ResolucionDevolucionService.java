package application.domain.services;

import application.domain.ports.in.ResolverDevolucionUseCase;

import application.domain.entities.Devolucion;
import application.domain.entities.Usuario;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;

public class ResolucionDevolucionService implements ResolverDevolucionUseCase {

    private final AutorizacionService autorizacion;

    public ResolucionDevolucionService(AutorizacionService autorizacion) {
        if (autorizacion == null)
            throw new IllegalArgumentException("El servicio de autorización es obligatorio.");
        this.autorizacion = autorizacion;
    }

    public void aprobar(Usuario actor, Devolucion devolucion) {
        validar(actor, devolucion);
        devolucion.aprobar();
    }

    public void rechazar(Usuario actor, Devolucion devolucion) {
        validar(actor, devolucion);
        devolucion.rechazar();
    }

    private void validar(Usuario actor, Devolucion devolucion) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.GestionDevoluciones);
        // El comprador solicita, pero no puede resolver su propia devolución.
        if (!actor.tieneRol(RolUsuario.Administrador))
            throw new AccesoDenegadoException("Solo un administrador puede resolver devoluciones.");
        if (devolucion == null)
            throw new IllegalArgumentException("La devolución es obligatoria.");
    }
}