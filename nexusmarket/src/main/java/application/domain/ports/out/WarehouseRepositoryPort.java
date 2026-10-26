package application.domain.ports.out;

import application.domain.entities.Warehouse;

public interface WarehouseRepositoryPort {

    boolean existsByName(String name);

    void save(Warehouse warehouse);
}