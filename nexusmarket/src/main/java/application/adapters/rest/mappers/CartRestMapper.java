package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.CartItemResponseDTO;
import application.adapters.rest.dtos.responses.CartResponseDTO;
import application.domain.entities.Cart;
import application.domain.entities.CartItem;

import java.math.BigDecimal;
import java.util.List;

/** The cart shows current prices; they are copied into the order only when it is confirmed. */
public final class CartRestMapper {

    private static final String DEFAULT_CURRENCY = "COP";

    private CartRestMapper() {
    }

    public static CartResponseDTO toResponseDTO(Cart cart) {
        List<CartItemResponseDTO> items = cart.getItems().stream().map(CartRestMapper::line).toList();
        BigDecimal total = items.stream().map(CartItemResponseDTO::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        String currency = cart.getItems().isEmpty() ? DEFAULT_CURRENCY
                : cart.getItems().get(0).getProduct().getPrice().currency();
        return new CartResponseDTO(cart.getId(), cart.getBuyer().getId(), items, total, currency);
    }

    private static CartItemResponseDTO line(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice().amount();
        return new CartItemResponseDTO(item.getProduct().getId(), item.getProduct().getName(), item.getQuantity(),
                unitPrice, unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}