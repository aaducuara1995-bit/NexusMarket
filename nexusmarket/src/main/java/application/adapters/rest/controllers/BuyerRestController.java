package application.adapters.rest.controllers;

import application.adapters.rest.RestReferenceLoader;
import application.adapters.rest.dtos.requests.AddToCartRequestDTO;
import application.adapters.rest.dtos.requests.ConfirmOrderRequestDTO;
import application.adapters.rest.dtos.requests.PaymentRequestDTO;
import application.adapters.rest.dtos.requests.ReturnRequestDTO;
import application.adapters.rest.dtos.responses.CartResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.adapters.rest.mappers.AddressRestMapper;
import application.adapters.rest.mappers.CartRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.ReturnRequestRestMapper;
import application.domain.entities.Order;
import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.ports.in.BuyerPort;
import application.domain.valueobjects.Money;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Endpoints of the BUYER role: cart, order, payment and returns. */
@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerRestController {

    private final BuyerPort buyerPort;
    private final RestReferenceLoader loader;

    public BuyerRestController(BuyerPort buyerPort,
                               RestReferenceLoader loader) {
        this.buyerPort = buyerPort;
        this.loader = loader;
    }

    @GetMapping("/cart")
    public ResponseEntity<CartResponseDTO> consultMyCart(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(CartRestMapper.toResponseDTO(buyerPort.consultMyCart(user)));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<CartResponseDTO> addToCart(@AuthenticationPrincipal(expression = "user") User user,
                                                     @Valid @RequestBody AddToCartRequestDTO request) {
        return ResponseEntity.ok(CartRestMapper.toResponseDTO(buyerPort.addToCart(user,
                loader.product(request.productId()), request.quantity())));
    }

    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeFromCart(@AuthenticationPrincipal(expression = "user") User user,
                                                          @PathVariable("productId") UUID productId) {
        return ResponseEntity.ok(CartRestMapper.toResponseDTO(
                buyerPort.removeFromCart(user, loader.product(productId))));
    }

    /** The body is optional: without deliveryAddress the order goes to the main address. */
    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> confirmOrder(@AuthenticationPrincipal(expression = "user") User user,
                                                         @Valid @RequestBody(required = false) ConfirmOrderRequestDTO request) {
        Order order = buyerPort.confirmOrder(user,
                request == null ? null : AddressRestMapper.toDomain(request.deliveryAddress()));
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderRestMapper.toResponseDTO(order));
    }

    /** Pays the order: the invoice is issued and a digital order is delivered at once. */
    @PostMapping("/orders/{orderId}/payment")
    public ResponseEntity<OrderResponseDTO> payOrder(@AuthenticationPrincipal(expression = "user") User user,
                                                     @PathVariable("orderId") UUID orderId,
                                                     @Valid @RequestBody PaymentRequestDTO request) {
        Money paid = request.currency() == null || request.currency().isBlank()
                ? new Money(request.amount())
                : new Money(request.amount(), request.currency());
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(
                buyerPort.payOrder(user, loader.order(orderId), paid)));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> consultMyOrders(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(buyerPort.consultMyOrders(user).stream()
                .map(OrderRestMapper::toResponseDTO).toList());
    }

    @PostMapping("/orders/{orderId}/returns")
    public ResponseEntity<ReturnRequestResponseDTO> requestReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @PathVariable("orderId") UUID orderId,
                                                                  @Valid @RequestBody ReturnRequestDTO request) {
        ReturnRequest created = buyerPort.requestReturn(user, loader.order(orderId), request.reason());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReturnRequestRestMapper.toResponseDTO(created, Optional.empty()));
    }
}