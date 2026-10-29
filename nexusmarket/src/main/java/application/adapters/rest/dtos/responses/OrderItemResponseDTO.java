package application.adapters.rest.dtos.responses;

import application.domain.enums.ProductType;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDTO(UUID productId, String productName, ProductType type, int quantity,
                                   BigDecimal unitPrice, BigDecimal subtotal) {
}