package application.domain.ports.in;

import application.domain.entities.Devolucion;
import application.domain.entities.Reembolso;

import java.util.List;

/** Puerto de entrada: genera el reembolso de una devolución aprobada. */
public interface GenerarReembolsoUseCase {

    Reembolso generar(Devolucion devolucion, List<Reembolso> existentes);
}