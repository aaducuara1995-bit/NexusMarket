package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.ProcesoNegocio;

import java.util.List;

public class CambioEstadoProductoService {

    private final AutorizacionService autorizacion;
    private final ValidacionVendedorService validacionVendedor;

    public CambioEstadoProductoService(AutorizacionService autorizacion,
                                       ValidacionVendedorService validacionVendedor) {
        if (autorizacion == null || validacionVendedor == null)
            throw new IllegalArgumentException("Los servicios de validaciÃ³n son obligatorios.");
        this.autorizacion = autorizacion;
        this.validacionVendedor = validacionVendedor;
    }

    public void suspender(Usuario actor, Vendedor vendedor, Producto producto) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.RegistroProductos);
        validacionVendedor.exigirPropietario(actor, vendedor, producto);

        producto.suspender();
    }

    public void descontinuar(Usuario actor, Vendedor vendedor, Producto producto,
                             List<Inventario> inventarios) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.RegistroProductos);
        validacionVendedor.exigirPropietario(actor, vendedor, producto);
        if (inventarios == null)
            throw new IllegalArgumentException("La lista de inventarios es obligatoria.");

        boolean hayReservasActivas = inventarios.stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .anyMatch(i -> i.getReservado() > 0);

        if (hayReservasActivas)
            throw new IllegalStateException(
                    "No se puede descontinuar un producto con reservas activas en pedidos en curso.");

        producto.descontinuar();
    }
}