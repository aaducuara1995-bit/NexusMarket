package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.SellerResponseDTO;
import application.domain.entities.Seller;
import application.domain.entities.Warehouse;

import java.util.List;

public final class SellerRestMapper {

    private SellerRestMapper() {
    }

    public static SellerResponseDTO toResponseDTO(Seller seller, List<Warehouse> warehouses) {
        return new SellerResponseDTO(seller.getId(), seller.getUserId(),
                warehouses.stream().map(WarehouseRestMapper::toResponseDTO).toList());
    }
}