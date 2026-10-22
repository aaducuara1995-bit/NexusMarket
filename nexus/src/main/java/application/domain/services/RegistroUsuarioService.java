package application.domain.services;

import application.domain.ports.in.RegistrarUsuarioUseCase;

import application.domain.entities.Usuario;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;
import application.domain.exceptions.AccesoDenegadoException;
import application.domain.ports.out.UsuarioRepositorioPort;
import application.domain.valueobjects.CorreoElectronico;
import application.domain.valueobjects.DocumentoIdentidad;
import application.domain.valueobjects.NombreCompleto;

public class RegistroUsuarioService implements RegistrarUsuarioUseCase {

    private final AutorizacionService autorizacion;
    private final UsuarioRepositorioPort usuarios;

    public RegistroUsuarioService(AutorizacionService autorizacion, UsuarioRepositorioPort usuarios) {
        if (autorizacion == null || usuarios == null)
            throw new IllegalArgumentException("Las dependencias del servicio son obligatorias.");
        this.autorizacion = autorizacion;
        this.usuarios = usuarios;
    }

    /**
     * Un comprador puede registrarse solo (actor nulo). Los vendedores no se auto-registran:
     * los registra un administrador, igual que a los demás roles internos.
     */
    public Usuario registrar(Usuario actor, NombreCompleto nombre, CorreoElectronico correo,
                             DocumentoIdentidad documento, RolUsuario rol) {
        if (rol == null)
            throw new IllegalArgumentException("El rol es obligatorio.");

        if (rol == RolUsuario.Vendedor) {
            if (actor == null)
                throw new AccesoDenegadoException("Los vendedores no se auto-registran.");
            autorizacion.exigirPermiso(actor, ProcesoNegocio.RegistroVendedores);
        } else if (rol != RolUsuario.Comprador) {
            if (actor == null || !actor.tieneRol(RolUsuario.Administrador) || !actor.estaActivo())
                throw new AccesoDenegadoException("Solo un administrador activo registra este rol.");
        }

        // El correo y el documento deben ser únicos en toda la plataforma.
        if (correo != null && usuarios.existePorCorreo(correo))
            throw new IllegalStateException("Ya existe un usuario con ese correo electrónico.");
        if (documento != null && usuarios.existePorDocumento(documento))
            throw new IllegalStateException("Ya existe un usuario con ese documento de identidad.");

        Usuario usuario = new Usuario(nombre, correo, documento, rol);
        usuarios.guardar(usuario);
        return usuario;
    }
}