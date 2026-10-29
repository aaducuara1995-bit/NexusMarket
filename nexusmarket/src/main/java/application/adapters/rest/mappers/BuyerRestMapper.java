package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.domain.entities.Buyer;

public final class BuyerRestMapper {

    private BuyerRestMapper() {
    }

    public static BuyerResponseDTO toResponseDTO(Buyer buyer) {
        return new BuyerResponseDTO(buyer.getId(), buyer.getUserId(),
                AddressRestMapper.toDTO(buyer.getMainAddress()),
                buyer.getAdditionalAddresses().stream().map(AddressRestMapper::toDTO).toList(),
                buyer.getCommercialStatus());
    }
}