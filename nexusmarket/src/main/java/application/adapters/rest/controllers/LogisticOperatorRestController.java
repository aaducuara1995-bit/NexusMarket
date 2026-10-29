package application.adapters.rest.controllers;

import application.adapters.rest.AuthenticatedUserResolver;
import application.adapters.rest.RestReferenceLoader;
import application.adapters.rest.dtos.requests.AdjustInventoryRequestDTO;
import application.adapters.rest.dtos.requests.InitialInventoryRequestDTO;
import application.adapters.rest.dtos.requests.RestockRequestDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.mappers.InventoryRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.domain.entities.Inventory;
import application.domain.ports.in.LogisticOperatorPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Endpoints of the LOGISTIC_OPERATOR role: warehouse stock of any seller and order shipping. */
@RestController
@RequestMapping("/api/v1/logistics")
public class LogisticOperatorRestController {

    private final LogisticOperatorPort logisticOperatorPort;
    private final AuthenticatedUserResolver currentUser;
    private final RestReferenceLoader loader;

    public LogisticOperatorRestController(LogisticOperatorPort logisticOperatorPort,
                                          AuthenticatedUserResolver currentUser, RestReferenceLoader loader) {
        this.logisticOperatorPort = logisticOperatorPort;
        this.currentUser = currentUser;
        this.loader = loader;
    }

    /** Paid orders with physical products, waiting in the warehouse. */
    @GetMapping("/orders/to-ship")
    public ResponseEntity<List<OrderResponseDTO>> consultOrdersToShip(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email) {
        return ResponseEntity.ok(logisticOperatorPort.consultOrdersToShip(currentUser.resolve(email)).stream()
                .map(OrderRestMapper::toResponseDTO).toList());
    }

    @PatchMapping("/orders/{orderId}/ship")
    public ResponseEntity<OrderResponseDTO> shipOrder(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                      @PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(
                logisticOperatorPort.shipOrder(currentUser.resolve(email), loader.order(orderId))));
    }

    @PatchMapping("/orders/{orderId}/deliver")
    public ResponseEntity<OrderResponseDTO> confirmDelivery(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                            @PathVariable("orderId") UUID orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(
                logisticOperatorPort.confirmDelivery(currentUser.resolve(email), loader.order(orderId))));
    }

    @PostMapping("/inventories")
    public ResponseEntity<InventoryResponseDTO> registerInitialInventory(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email,
            @Valid @RequestBody InitialInventoryRequestDTO request) {
        Inventory inventory = logisticOperatorPort.registerInitialInventory(currentUser.resolve(email),
                loader.product(request.productId()), loader.warehouse(request.warehouseId()), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryRestMapper.toResponseDTO(inventory));
    }

    @PatchMapping("/inventories/{inventoryId}/adjustment")
    public ResponseEntity<InventoryResponseDTO> adjustInventory(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                                @PathVariable("inventoryId") UUID inventoryId,
                                                                @Valid @RequestBody AdjustInventoryRequestDTO request) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(logisticOperatorPort.adjustInventory(
                currentUser.resolve(email), loader.inventory(inventoryId), request.difference(), request.reason())));
    }

    @PatchMapping("/inventories/{inventoryId}/damaged")
    public ResponseEntity<InventoryResponseDTO> markInventoryDamaged(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email,
            @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                logisticOperatorPort.markInventoryDamaged(currentUser.resolve(email), loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/block")
    public ResponseEntity<InventoryResponseDTO> blockInventory(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                               @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                logisticOperatorPort.blockInventory(currentUser.resolve(email), loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/enable")
    public ResponseEntity<InventoryResponseDTO> enableInventory(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                                @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                logisticOperatorPort.enableInventory(currentUser.resolve(email), loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/restock")
    public ResponseEntity<InventoryResponseDTO> restockFromReturn(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                                  @PathVariable("inventoryId") UUID inventoryId,
                                                                  @Valid @RequestBody RestockRequestDTO request) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(logisticOperatorPort.restockFromReturn(
                currentUser.resolve(email), loader.inventory(inventoryId),
                loader.returnRequest(request.returnId()), request.units())));
    }
}