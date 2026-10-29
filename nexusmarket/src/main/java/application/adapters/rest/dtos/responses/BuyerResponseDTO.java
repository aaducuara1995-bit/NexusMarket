package application.adapters.rest.dtos.responses;

import application.adapters.rest.dtos.AddressDTO;
import application.domain.enums.CommercialStatus;

import java.util.List;
import java.util.UUID;

public record BuyerResponseDTO(UUID id, UUID userId, AddressDTO mainAddress,
                               List<AddressDTO> additionalAddresses, CommercialStatus commercialStatus) {
}