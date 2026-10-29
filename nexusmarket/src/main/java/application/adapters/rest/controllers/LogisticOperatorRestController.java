package application.adapters.rest.controllers;

import application.adapters.rest.RestReferenceLoader;
import application.adapters.rest.dtos.requests.AdjustInventoryRequestDTO;
import application.adapters.rest.dtos.requests.InitialInventoryRequestDTO;
import application.adapters.rest.dtos.requests.RestockRequestDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.mappers.InventoryRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.domain.entities.Inventory;
import application.domain.entities.User;
import application.domain.ports.in.LogisticOperatorPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Endpoints of the LOGISTIC_OPERATOR role: warehouse stock of any seller and order shipping. */
@RestController
@RequestMapping("/api/v1/logistics")
public class LogisticOperatorRestController {

    private final LogisticOperatorPort logisticOperatorPort;
    private final RestReferenceLoader loader;

    public LogisticOperatorRestController(LogisticOperatorPort logisticOperatorPort, RestReferenceLoader loader) {
        this.logisticOperatorPort = logisticOperatorPort;
        this.loader = loader;
    }

    /** Paid orders with physical products, waiting in the warehouse. */
    @GetMapping("/orders/to-ship")
    public ResponseEntity<List<OrderResponseDTO>> consultOrdersToShip(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(logisticOperatorPort.consultOrdersToShip(user).stream()
                .map(OrderRestMapper::toResponseDTO).toList());
    }

    @PatchMapping("/orders/{orderId}/ship")
    public ResponseEntity<OrderResponseDTO> shipOrder(@AuthenticationPrincipal(expression = "user") User user,
                                                      @PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(
                logisticOperatorPort.shipOrder(user, loader.order(orderId))));
    }

    @PatchMapping("/orders/{orderId}/deliver")
    public ResponseEntity<OrderResponseDTO> confirmDelivery(@AuthenticationPrincipal(expression = "user") User user,
                                                            @PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(
                logisticOperatorPort.confirmDelivery(user, loader.order(orderId))));
    }

    @PostMapping("/inventories")
    public ResponseEntity<InventoryResponseDTO> registerInitialInventory(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody InitialInventoryRequestDTO request) {
        Inventory inventory = logisticOperatorPort.registerInitialInventory(user,
                loader.product(request.productId()), loader.warehouse(request.warehouseId()), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryRestMapper.toResponseDTO(inventory));
    }

    @PatchMapping("/inventories/{inventoryId}/adjustment")
    public ResponseEntity<InventoryResponseDTO> adjustInventory(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable("inventoryId") UUID inventoryId,
                                                                @Valid @RequestBody AdjustInventoryRequestDTO request) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(logisticOperatorPort.adjustInventory(
                user, loader.inventory(inventoryId), request.difference(), request.reason())));
    }

    @PatchMapping("/inventories/{inventoryId}/damaged")
    public ResponseEntity<InventoryResponseDTO> markInventoryDamaged(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                logisticOperatorPort.markInventoryDamaged(user, loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/block")
    public ResponseEntity<InventoryResponseDTO> blockInventory(@AuthenticationPrincipal(expression = "user") User user,
                                                               @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                logisticOperatorPort.blockInventory(user, loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/enable")
    public ResponseEntity<InventoryResponseDTO> enableInventory(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                logisticOperatorPort.enableInventory(user, loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/restock")
    public ResponseEntity<InventoryResponseDTO> restockFromReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @PathVariable("inventoryId") UUID inventoryId,
                                                                  @Valid @RequestBody RestockRequestDTO request) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(logisticOperatorPort.restockFromReturn(
                user, loader.inventory(inventoryId),
                loader.returnRequest(request.returnId()), request.units())));
    }
}