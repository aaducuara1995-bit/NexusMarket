package application.domain.services;

import application.domain.ports.in.RegistrarInventarioInicialUseCase;

import application.domain.entities.Bodega;
import application.domain.entities.Inventario;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Producto;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.RolUsuario;
import application.domain.enums.TipoMovimientoInventario;
import application.domain.exceptions.AccesoDenegadoException;

import java.util.ArrayList;
import java.util.List;

public class RegistroInventarioInicialService implements RegistrarInventarioInicialUseCase {

    public record Resultado(Inventario inventario, List<MovimientoInventario> movimientos) {
    }

    private final AccesoInventarioService acceso;

    public RegistroInventarioInicialService(AccesoInventarioService acceso) {
        if (acceso == null)
            throw new IllegalArgumentException("El servicio de acceso es obligatorio.");
        this.acceso = acceso;
    }

    public Resultado registrar(Usuario actor, Vendedor vendedor, Producto producto,
                               Bodega bodega, int cantidadInicial, List<Inventario> existentes) {
        acceso.exigirAcceso(actor, vendedor, producto);
        if (bodega == null)
            throw new IllegalArgumentException("La bodega es obligatoria.");
        if (existentes == null)
            throw new IllegalArgumentException("La lista de inventarios existentes es obligatoria.");

        if (!producto.esFisico())
            throw new IllegalStateException("Solo los productos físicos llevan inventario.");

        if (actor.tieneRol(RolUsuario.Vendedor)
                && !(bodega.esDelMarketplace() || bodega.perteneceA(vendedor.getId())))
            throw new AccesoDenegadoException(
                    "El vendedor solo puede almacenar en sus bodegas o en las del Marketplace.");

        boolean yaExiste = existentes.stream()
                .anyMatch(i -> i.esDe(producto.getId(), bodega.getId()));
        if (yaExiste)
            throw new IllegalStateException("Ya existe inventario de este producto en esa bodega.");

        Inventario inventario = new Inventario(producto, bodega, cantidadInicial);

        List<MovimientoInventario> movimientos = new ArrayList<>();
        if (cantidadInicial > 0)
            movimientos.add(new MovimientoInventario(
                    inventario, TipoMovimientoInventario.Ingreso, cantidadInicial,
                    "Existencias iniciales"));

        return new Resultado(inventario, movimientos);
    }
}