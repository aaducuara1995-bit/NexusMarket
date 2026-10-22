package application.domain.services;

import application.domain.ports.in.RegistrarProductoUseCase;

import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.ProcesoNegocio;
import application.domain.enums.TipoProducto;
import application.domain.ports.out.ProductoRepositorioPort;
import application.domain.valueobjects.Dinero;

public class RegistroProductoService implements RegistrarProductoUseCase {

    private final AutorizacionService autorizacion;
    private final ValidacionVendedorService validacionVendedor;
    private final ProductoRepositorioPort productos;

    public RegistroProductoService(AutorizacionService autorizacion,
                                   ValidacionVendedorService validacionVendedor,
                                   ProductoRepositorioPort productos) {
        if (autorizacion == null || validacionVendedor == null || productos == null)
            throw new IllegalArgumentException("Las dependencias del servicio son obligatorias.");
        this.autorizacion = autorizacion;
        this.validacionVendedor = validacionVendedor;
        this.productos = productos;
    }

    public Producto registrar(Usuario actor, Vendedor vendedor, String nombre, String descripcion,
                              TipoProducto tipo, Dinero precio) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.RegistroProductos);
        validacionVendedor.exigirVendedor(actor, vendedor);

        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        if (productos.existeNombreParaVendedor(vendedor.getId(), nombre.trim()))
            throw new IllegalStateException("El vendedor ya tiene un producto con ese nombre.");

        // El producto nace suspendido; se publica después con PublicacionProductoService.
        Producto producto = new Producto(vendedor.getId(), nombre.trim(), descripcion, tipo, precio);
        productos.guardar(producto);
        return producto;
    }
}