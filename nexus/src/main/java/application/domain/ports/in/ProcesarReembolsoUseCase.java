package application.domain.ports.in;

import application.domain.entities.Reembolso;
import application.domain.entities.Usuario;

/** Puerto de entrada: el administrador procesa un reembolso pendiente. */
public interface ProcesarReembolsoUseCase {

    void procesar(Usuario actor, Reembolso reembolso);
}