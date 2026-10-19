package application.domain.services;

import application.domain.entities.Bodega;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoBodega;
import application.domain.valueobjects.Direccion;

public class IncorporacionVendedorService {

    public record Resultado(Vendedor vendedor, Bodega bodega) {
    }

    private final AutorizacionService autorizacion;

    public IncorporacionVendedorService(AutorizacionService autorizacion) {
        if (autorizacion == null)
            throw new IllegalArgumentException("El servicio de autorizaciÃ³n es obligatorio.");
        this.autorizacion = autorizacion;
    }

    public Resultado incorporar(Usuario administrador,
                                Usuario usuarioVendedor,
                                String nombreBodega,
                                TipoBodega tipoBodega,
                                Direccion direccionBodega) {
        // Los vendedores no se auto-registran: los incorpora un administrador,
        // junto con su primera bodega (flujo 6.1 de la especificaciÃ³n).
        autorizacion.exigirPermiso(administrador, ProcesoNegocio.RegistroVendedores);
        autorizacion.exigirPermiso(administrador, ProcesoNegocio.AdministracionBodegas);

        if (usuarioVendedor == null)
            throw new IllegalArgumentException("El usuario a incorporar es obligatorio.");
        if (!usuarioVendedor.tieneRol(RolUsuario.Vendedor))
            throw new IllegalStateException("Solo se puede incorporar a un usuario con rol de vendedor.");
        if (!usuarioVendedor.estaActivo())
            throw new IllegalStateException("No se puede incorporar a un usuario bloqueado.");

        Vendedor vendedor = new Vendedor(usuarioVendedor.getId());
        Bodega bodega = Bodega.deVendedor(vendedor.getId(), nombreBodega, tipoBodega, direccionBodega);

        return new Resultado(vendedor, bodega);
    }
}