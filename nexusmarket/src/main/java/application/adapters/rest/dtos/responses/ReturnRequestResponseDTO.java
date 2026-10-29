package application.adapters.rest.dtos.responses;

import application.domain.enums.ReturnStatus;

import java.util.UUID;

/** A return request. refundId is filled in once the return is approved and the refund exists. */
public record ReturnRequestResponseDTO(UUID id, UUID orderId, String reason, ReturnStatus status, UUID refundId) {
}