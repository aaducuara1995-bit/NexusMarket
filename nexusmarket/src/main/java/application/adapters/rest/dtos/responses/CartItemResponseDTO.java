package application.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponseDTO(UUID productId, String productName, int quantity,
                                  BigDecimal unitPrice, BigDecimal subtotal) {
}