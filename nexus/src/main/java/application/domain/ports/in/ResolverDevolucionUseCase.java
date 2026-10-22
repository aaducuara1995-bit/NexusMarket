package application.domain.ports.in;

import application.domain.entities.Devolucion;
import application.domain.entities.Usuario;

/** Puerto de entrada: el administrador aprueba o rechaza una devolución. */
public interface ResolverDevolucionUseCase {

    void aprobar(Usuario actor, Devolucion devolucion);

    void rechazar(Usuario actor, Devolucion devolucion);
}