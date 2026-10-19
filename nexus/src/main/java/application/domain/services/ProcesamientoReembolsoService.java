package application.domain.services;

import application.domain.entities.Reembolso;
import application.domain.entities.Usuario;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;

public class ProcesamientoReembolsoService {

    private final AutorizacionService autorizacion;

    public ProcesamientoReembolsoService(AutorizacionService autorizacion) {
        if (autorizacion == null)
            throw new IllegalArgumentException("El servicio de autorización es obligatorio.");
        this.autorizacion = autorizacion;
    }

    public void procesar(Usuario actor, Reembolso reembolso) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.GestionReembolsos);
        // El comprador participa del reembolso, pero quien lo ejecuta es el administrador.
        if (!actor.tieneRol(RolUsuario.Administrador))
            throw new AccesoDenegadoException("Solo un administrador puede procesar reembolsos.");
        if (reembolso == null)
            throw new IllegalArgumentException("El reembolso es obligatorio.");

        reembolso.procesar();
    }
}