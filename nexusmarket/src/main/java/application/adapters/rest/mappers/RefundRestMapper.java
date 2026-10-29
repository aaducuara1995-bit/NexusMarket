package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.RefundResponseDTO;
import application.domain.entities.Refund;

public final class RefundRestMapper {

    private RefundRestMapper() {
    }

    public static RefundResponseDTO toResponseDTO(Refund refund) {
        return new RefundResponseDTO(refund.getId(), refund.getOrder().getId(), refund.getAmount().amount(),
                refund.getAmount().currency(), refund.getStatus());
    }
}