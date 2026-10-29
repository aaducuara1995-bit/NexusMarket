package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;

import java.util.Optional;

public final class ReturnRequestRestMapper {

    private ReturnRequestRestMapper() {
    }

    public static ReturnRequestResponseDTO toResponseDTO(ReturnRequest returnRequest, Optional<Refund> refund) {
        return new ReturnRequestResponseDTO(returnRequest.getId(), returnRequest.getOrder().getId(),
                returnRequest.getReason(), returnRequest.getStatus(), refund.map(Refund::getId).orElse(null));
    }
}