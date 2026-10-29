package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.WarehouseRequestDTO;
import application.adapters.rest.dtos.responses.WarehouseResponseDTO;
import application.domain.entities.Warehouse;

/**
 * The request becomes a draft marketplace warehouse; the admin use case reads its name,
 * type and address and decides the real owner (marketplace or seller).
 */
public final class WarehouseRestMapper {

    private WarehouseRestMapper() {
    }

    public static Warehouse toDraft(WarehouseRequestDTO dto) {
        return Warehouse.ofMarketplace(dto.name(), dto.type(), AddressRestMapper.toDomain(dto.address()));
    }

    public static WarehouseResponseDTO toResponseDTO(Warehouse warehouse) {
        return new WarehouseResponseDTO(warehouse.getId(), warehouse.getName(), warehouse.getType(),
                AddressRestMapper.toDTO(warehouse.getAddress()), warehouse.getOwnership(), warehouse.getSellerId());
    }
}