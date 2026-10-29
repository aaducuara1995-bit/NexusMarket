package application.adapters.rest.dtos.responses;

import java.util.UUID;

/** Units of a product available to buy, adding up every warehouse. */
public record AvailabilityResponseDTO(UUID productId, int available) {
}