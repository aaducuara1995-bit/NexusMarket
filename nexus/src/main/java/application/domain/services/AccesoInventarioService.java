package application.domain.services;

import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.RolUsuario;

public class AccesoInventarioService {

    private final AutorizacionService autorizacion;
    private final ValidacionVendedorService validacionVendedor;

    public AccesoInventarioService(AutorizacionService autorizacion,
                                   ValidacionVendedorService validacionVendedor) {
        if (autorizacion == null || validacionVendedor == null)
            throw new IllegalArgumentException("Los servicios de validaciÃ³n son obligatorios.");
        this.autorizacion = autorizacion;
        this.validacionVendedor = validacionVendedor;
    }

    /**
     * El vendedor solo administra el inventario de sus propios productos.
     * El operador logÃ­stico opera las bodegas y no necesita ser dueÃ±o del producto.
     */
    public void exigirAcceso(Usuario actor, Vendedor vendedor, Producto producto) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.AdministracionInventario);
        if (producto == null)
            throw new IllegalArgumentException("El producto es obligatorio.");

        if (actor.tieneRol(RolUsuario.Vendedor))
            validacionVendedor.exigirPropietario(actor, vendedor, producto);
    }
}