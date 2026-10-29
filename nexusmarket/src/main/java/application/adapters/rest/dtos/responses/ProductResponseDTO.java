package application.adapters.rest.dtos.responses;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductResponseDTO(UUID id, UUID sellerId, String name, String description, ProductType type,
                                 BigDecimal price, String currency, ProductStatus status, List<String> variants) {
}