package application.adapters.rest.dtos.responses;

import application.domain.enums.RefundStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundResponseDTO(UUID id, UUID orderId, BigDecimal amount, String currency, RefundStatus status) {
}