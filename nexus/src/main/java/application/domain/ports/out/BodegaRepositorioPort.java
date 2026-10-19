package application.domain.ports.out;

import application.domain.entities.Bodega;

public interface BodegaRepositorioPort {

    boolean existePorNombre(String nombre);

    void guardar(Bodega bodega);
}