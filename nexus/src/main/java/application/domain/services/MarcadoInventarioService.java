package application.domain.services;

import application.domain.ports.in.MarcarInventarioUseCase;

import application.domain.entities.Inventario;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;

public class MarcadoInventarioService implements MarcarInventarioUseCase {

    private final AccesoInventarioService acceso;

    public MarcadoInventarioService(AccesoInventarioService acceso) {
        if (acceso == null)
            throw new IllegalArgumentException("El servicio de acceso es obligatorio.");
        this.acceso = acceso;
    }

    public void marcarDanado(Usuario actor, Vendedor vendedor, Inventario inventario) {
        validar(actor, vendedor, inventario);
        inventario.marcarDanado();
    }

    public void bloquear(Usuario actor, Vendedor vendedor, Inventario inventario) {
        validar(actor, vendedor, inventario);
        inventario.bloquear();
    }

    public void habilitar(Usuario actor, Vendedor vendedor, Inventario inventario) {
        validar(actor, vendedor, inventario);
        inventario.habilitar();
    }

    private void validar(Usuario actor, Vendedor vendedor, Inventario inventario) {
        if (inventario == null)
            throw new IllegalArgumentException("El inventario es obligatorio.");
        acceso.exigirAcceso(actor, vendedor, inventario.getProducto());
    }
}