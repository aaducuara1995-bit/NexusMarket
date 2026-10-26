package application.domain.ports.out;

import application.domain.entities.ReturnRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRequestRepositoryPort {

    Optional<ReturnRequest> findById(UUID id);

    List<ReturnRequest> findByOrder(UUID orderId);

    void save(ReturnRequest returnRequest);
}