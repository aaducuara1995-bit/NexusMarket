package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.OrderItemResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.domain.entities.Order;
import application.domain.valueobjects.Money;

public final class OrderRestMapper {

    private OrderRestMapper() {
    }

    public static OrderResponseDTO toResponseDTO(Order order) {
        Money total = order.calculateTotal();
        return new OrderResponseDTO(order.getId(), order.getBuyer().getId(), order.getDate(), order.getStatus(),
                AddressRestMapper.toDTO(order.getDeliveryAddress()),
                order.getItems().stream()
                        .map(item -> new OrderItemResponseDTO(item.getProductId(), item.getProductName(),
                                item.getType(), item.getQuantity(), item.getUnitPrice().amount(),
                                item.subtotal().amount()))
                        .toList(),
                total.amount(), total.currency());
    }
}