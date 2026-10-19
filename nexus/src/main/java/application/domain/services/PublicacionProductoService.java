package application.domain.services;

import application.domain.entities.Inventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.ProcesoNegocio;

import java.util.List;

public class PublicacionProductoService {

    private final AutorizacionService autorizacion;
    private final ValidacionVendedorService validacionVendedor;
    private final ConsultaDisponibilidadService consulta = new ConsultaDisponibilidadService();

    public PublicacionProductoService(AutorizacionService autorizacion,
                                      ValidacionVendedorService validacionVendedor) {
        if (autorizacion == null || validacionVendedor == null)
            throw new IllegalArgumentException("Los servicios de validaciÃ³n son obligatorios.");
        this.autorizacion = autorizacion;
        this.validacionVendedor = validacionVendedor;
    }

    public void publicar(Usuario actor, Vendedor vendedor, Producto producto,
                         List<Inventario> inventarios) {
        autorizacion.exigirPermiso(actor, ProcesoNegocio.RegistroProductos);
        validacionVendedor.exigirPropietario(actor, vendedor, producto);
        if (inventarios == null)
            throw new IllegalArgumentException("La lista de inventarios es obligatoria.");

        // Los productos fÃ­sicos requieren existencias; los digitales no (se entregan tras el pago).
        if (producto.esFisico() && !consulta.hayDisponibilidad(producto, 1, inventarios))
            throw new IllegalStateException(
                    "No se puede publicar un producto fÃ­sico sin existencias disponibles.");

        producto.publicar();
    }
}