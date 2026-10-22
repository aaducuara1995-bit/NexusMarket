package application.domain.services;

import application.domain.ports.in.AjustarInventarioUseCase;

import application.domain.entities.Inventario;
import application.domain.entities.MovimientoInventario;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;
import application.domain.enums.TipoMovimientoInventario;

public class AjusteInventarioService implements AjustarInventarioUseCase {

    private final AccesoInventarioService acceso;

    public AjusteInventarioService(AccesoInventarioService acceso) {
        if (acceso == null)
            throw new IllegalArgumentException("El servicio de acceso es obligatorio.");
        this.acceso = acceso;
    }

    public MovimientoInventario ajustar(Usuario actor, Vendedor vendedor, Inventario inventario,
                                        int diferencia, String motivo) {
        if (inventario == null)
            throw new IllegalArgumentException("El inventario es obligatorio.");
        acceso.exigirAcceso(actor, vendedor, inventario.getProducto());

        // Se valida el motivo antes de modificar el inventario.
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("El motivo del ajuste es obligatorio.");

        inventario.ajustar(diferencia);

        return new MovimientoInventario(
                inventario, TipoMovimientoInventario.Ajuste, diferencia,
                "Ajuste: " + motivo.trim());
    }
}