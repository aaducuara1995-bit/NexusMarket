package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.domain.entities.Product;

public final class ProductRestMapper {

    private ProductRestMapper() {
    }

    public static ProductResponseDTO toResponseDTO(Product product) {
        return new ProductResponseDTO(product.getId(), product.getSellerId(), product.getName(),
                product.getDescription(), product.getType(), product.getPrice().amount(),
                product.getPrice().currency(), product.getStatus(), product.getVariants());
    }
}