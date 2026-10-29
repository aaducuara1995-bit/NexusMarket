package application.adapters.rest.dtos.responses;

import application.adapters.rest.dtos.AddressDTO;
import application.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(UUID id, UUID buyerId, LocalDateTime date, OrderStatus status,
                               AddressDTO deliveryAddress, List<OrderItemResponseDTO> items,
                               BigDecimal total, String currency) {
}