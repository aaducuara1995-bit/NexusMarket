package application.adapters.rest.controllers;

import application.adapters.rest.RestReferenceLoader;
import application.adapters.rest.dtos.requests.AdjustInventoryRequestDTO;
import application.adapters.rest.dtos.requests.InitialInventoryRequestDTO;
import application.adapters.rest.dtos.requests.ProductRequestDTO;
import application.adapters.rest.dtos.requests.RestockRequestDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.mappers.InventoryRestMapper;
import application.adapters.rest.mappers.ProductRestMapper;
import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.ports.in.SellerPort;
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

/** Endpoints of the SELLER role: their catalog and the stock of their products. */
@RestController
@RequestMapping("/api/v1/seller")
public class SellerRestController {

    private final SellerPort sellerPort;
    private final RestReferenceLoader loader;

    public SellerRestController(SellerPort sellerPort,
                                RestReferenceLoader loader) {
        this.sellerPort = sellerPort;
        this.loader = loader;
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponseDTO> registerProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                              @Valid @RequestBody ProductRequestDTO request) {
        Product product = sellerPort.registerProduct(user, ProductRestMapper.toDraft(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductRestMapper.toResponseDTO(product));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDTO>> consultMyProducts(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(sellerPort.consultMyProducts(user).stream()
                .map(ProductRestMapper::toResponseDTO).toList());
    }

    @PatchMapping("/products/{productId}/publish")
    public ResponseEntity<ProductResponseDTO> publishProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                             @PathVariable("productId") UUID productId) {
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(
                sellerPort.publishProduct(user, loader.product(productId))));
    }

    @PatchMapping("/products/{productId}/suspend")
    public ResponseEntity<ProductResponseDTO> suspendProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                             @PathVariable("productId") UUID productId) {
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(
                sellerPort.suspendProduct(user, loader.product(productId))));
    }

    @PatchMapping("/products/{productId}/discontinue")
    public ResponseEntity<ProductResponseDTO> discontinueProduct(@AuthenticationPrincipal(expression = "user") User user,
                                                                 @PathVariable("productId") UUID productId) {
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(
                sellerPort.discontinueProduct(user, loader.product(productId))));
    }

    @PostMapping("/inventories")
    public ResponseEntity<InventoryResponseDTO> registerInitialInventory(
            @AuthenticationPrincipal(expression = "user") User user,
            @Valid @RequestBody InitialInventoryRequestDTO request) {
        Inventory inventory = sellerPort.registerInitialInventory(user,
                loader.product(request.productId()), loader.warehouse(request.warehouseId()), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(InventoryRestMapper.toResponseDTO(inventory));
    }

    @PatchMapping("/inventories/{inventoryId}/adjustment")
    public ResponseEntity<InventoryResponseDTO> adjustInventory(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable("inventoryId") UUID inventoryId,
                                                                @Valid @RequestBody AdjustInventoryRequestDTO request) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(sellerPort.adjustInventory(
                user, loader.inventory(inventoryId), request.difference(), request.reason())));
    }

    @PatchMapping("/inventories/{inventoryId}/damaged")
    public ResponseEntity<InventoryResponseDTO> markInventoryDamaged(
            @AuthenticationPrincipal(expression = "user") User user,
            @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                sellerPort.markInventoryDamaged(user, loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/block")
    public ResponseEntity<InventoryResponseDTO> blockInventory(@AuthenticationPrincipal(expression = "user") User user,
                                                               @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                sellerPort.blockInventory(user, loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/enable")
    public ResponseEntity<InventoryResponseDTO> enableInventory(@AuthenticationPrincipal(expression = "user") User user,
                                                                @PathVariable("inventoryId") UUID inventoryId) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(
                sellerPort.enableInventory(user, loader.inventory(inventoryId))));
    }

    @PatchMapping("/inventories/{inventoryId}/restock")
    public ResponseEntity<InventoryResponseDTO> restockFromReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @PathVariable("inventoryId") UUID inventoryId,
                                                                  @Valid @RequestBody RestockRequestDTO request) {
        return ResponseEntity.ok(InventoryRestMapper.toResponseDTO(sellerPort.restockFromReturn(
                user, loader.inventory(inventoryId),
                loader.returnRequest(request.returnId()), request.units())));
    }
}