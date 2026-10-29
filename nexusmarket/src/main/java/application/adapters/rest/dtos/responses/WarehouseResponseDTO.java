package application.adapters.rest.dtos.responses;

import application.adapters.rest.dtos.AddressDTO;
import application.domain.enums.WarehouseOwnership;
import application.domain.enums.WarehouseType;

import java.util.UUID;

public record WarehouseResponseDTO(UUID id, String name, WarehouseType type, AddressDTO address,
                                   WarehouseOwnership ownership, UUID sellerId) {
}