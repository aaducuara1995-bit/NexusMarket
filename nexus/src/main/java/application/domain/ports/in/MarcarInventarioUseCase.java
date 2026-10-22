package application.domain.ports.in;

import application.domain.entities.Inventario;
import application.domain.entities.Usuario;
import application.domain.entities.Vendedor;

/** Puerto de entrada: marca un inventario como dañado, lo bloquea o lo vuelve a habilitar. */
public interface MarcarInventarioUseCase {

    void marcarDanado(Usuario actor, Vendedor vendedor, Inventario inventario);

    void bloquear(Usuario actor, Vendedor vendedor, Inventario inventario);

    void habilitar(Usuario actor, Vendedor vendedor, Inventario inventario);
}