package application.domain.services;

import application.domain.ports.in.RegistrarVendedorUseCase;

import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;
import application.domain.ports.out.VendedorRepositorioPort;

public class RegistroVendedorService implements RegistrarVendedorUseCase {

    private final AutorizacionService autorizacion;
    private final VendedorRepositorioPort vendedores;

    public RegistroVendedorService(AutorizacionService autorizacion, VendedorRepositorioPort vendedores) {
        if (autorizacion == null || vendedores == null)
            throw new IllegalArgumentException("Las dependencias del servicio son obligatorias.");
        this.autorizacion = autorizacion;
        this.vendedores = vendedores;
    }

    public Vendedor registrar(Usuario administrador, Usuario usuarioVendedor) {
        autorizacion.exigirPermiso(administrador, ProcesoNegocio.RegistroVendedores);
        if (usuarioVendedor == null)
            throw new IllegalArgumentException("El usuario a registrar es obligatorio.");
        if (!usuarioVendedor.tieneRol(RolUsuario.Vendedor))
            throw new IllegalStateException("El usuario no tiene el rol de vendedor.");
        if (!usuarioVendedor.estaActivo())
            throw new IllegalStateException("No se puede registrar a un usuario bloqueado.");
        if (vendedores.existePorUsuarioId(usuarioVendedor.getId()))
            throw new IllegalStateException("El usuario ya tiene un perfil de vendedor.");

        Vendedor vendedor = new Vendedor(usuarioVendedor.getId());
        vendedores.guardar(vendedor);
        return vendedor;
    }
}