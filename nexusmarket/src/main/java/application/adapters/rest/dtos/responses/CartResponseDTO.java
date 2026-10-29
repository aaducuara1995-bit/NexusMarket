package application.adapters.rest.dtos.responses;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponseDTO(UUID id, UUID buyerId, List<CartItemResponseDTO> items,
                              BigDecimal total, String currency) {
}