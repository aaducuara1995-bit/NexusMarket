package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.ProductRequestDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.domain.entities.Product;
import application.domain.valueobjects.Money;

import java.util.UUID;

public final class ProductRestMapper {

    private ProductRestMapper() {
    }

    /**
     * The request becomes a draft product. Its seller id is a placeholder: the seller use
     * case registers the product under the signed-in seller, whatever the draft says.
     */
    public static Product toDraft(ProductRequestDTO dto) {
        Money price = dto.currency() == null || dto.currency().isBlank()
                ? new Money(dto.price())
                : new Money(dto.price(), dto.currency());
        Product draft = new Product(new UUID(0L, 0L), dto.name(), dto.description(), dto.type(), price);
        if (dto.variants() != null)
            dto.variants().forEach(draft::addVariant);
        return draft;
    }

    public static ProductResponseDTO toResponseDTO(Product product) {
        return new ProductResponseDTO(product.getId(), product.getSellerId(), product.getName(),
                product.getDescription(), product.getType(), product.getPrice().amount(),
                product.getPrice().currency(), product.getStatus(), product.getVariants());
    }
}