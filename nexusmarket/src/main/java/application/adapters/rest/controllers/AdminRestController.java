package application.adapters.rest.controllers;

import application.adapters.rest.AuthenticatedUserResolver;
import application.adapters.rest.RestReferenceLoader;
import application.adapters.rest.dtos.requests.OnboardSellerRequestDTO;
import application.adapters.rest.dtos.requests.RegisterUserRequestDTO;
import application.adapters.rest.dtos.requests.WarehouseRequestDTO;
import application.adapters.rest.dtos.responses.AuditLogResponseDTO;
import application.adapters.rest.dtos.responses.RefundResponseDTO;
import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.adapters.rest.dtos.responses.SellerResponseDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.adapters.rest.dtos.responses.WarehouseResponseDTO;
import application.adapters.rest.mappers.AuditLogRestMapper;
import application.adapters.rest.mappers.RefundRestMapper;
import application.adapters.rest.mappers.ReturnRequestRestMapper;
import application.adapters.rest.mappers.SellerRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.adapters.rest.mappers.WarehouseRestMapper;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.ports.in.AdminPort;
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
import java.util.Optional;
import java.util.UUID;

/** Endpoints of the ADMIN role: users, sellers, warehouses, returns, refunds and audit. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminRestController {

    private final AdminPort adminPort;
    private final AuthenticatedUserResolver currentUser;
    private final RestReferenceLoader loader;

    public AdminRestController(AdminPort adminPort, AuthenticatedUserResolver currentUser,
                               RestReferenceLoader loader) {
        this.adminPort = adminPort;
        this.currentUser = currentUser;
        this.loader = loader;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                        @Valid @RequestBody RegisterUserRequestDTO request) {
        User registered = adminPort.registerUser(currentUser.resolve(email), UserRestMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserRestMapper.toResponseDTO(registered));
    }

    @PatchMapping("/users/{userId}/block")
    public ResponseEntity<UserResponseDTO> blockUser(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                     @PathVariable("userId") UUID userId) {
        User blocked = adminPort.blockUser(currentUser.resolve(email), loader.user(userId));
        return ResponseEntity.ok(UserRestMapper.toResponseDTO(blocked));
    }

    @PatchMapping("/users/{userId}/activate")
    public ResponseEntity<UserResponseDTO> activateUser(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                        @PathVariable("userId") UUID userId) {
        User active = adminPort.activateUser(currentUser.resolve(email), loader.user(userId));
        return ResponseEntity.ok(UserRestMapper.toResponseDTO(active));
    }

    @PostMapping("/sellers")
    public ResponseEntity<SellerResponseDTO> onboardSeller(@RequestHeader(AuthenticatedUserResolver.HEADER) String email,
                                                           @Valid @RequestBody OnboardSellerRequestDTO request) {
        Seller seller = adminPort.onboardSeller(currentUser.resolve(email), loader.user(request.userId()),
                WarehouseRestMapper.toDraft(request.firstWarehouse()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SellerRestMapper.toResponseDTO(seller, loader.warehousesOfSeller(seller.getId())));
    }

    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponseDTO> registerMarketplaceWarehouse(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email,
            @Valid @RequestBody WarehouseRequestDTO request) {
        Warehouse warehouse = adminPort.registerMarketplaceWarehouse(currentUser.resolve(email),
                WarehouseRestMapper.toDraft(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(WarehouseRestMapper.toResponseDTO(warehouse));
    }

    @PostMapping("/sellers/{sellerId}/warehouses")
    public ResponseEntity<WarehouseResponseDTO> registerSellerWarehouse(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email,
            @PathVariable("sellerId") UUID sellerId,
            @Valid @RequestBody WarehouseRequestDTO request) {
        Warehouse warehouse = adminPort.registerSellerWarehouse(currentUser.resolve(email),
                loader.seller(sellerId), WarehouseRestMapper.toDraft(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(WarehouseRestMapper.toResponseDTO(warehouse));
    }

    /** Approving also generates the refund; its id comes back in refundId. */
    @PatchMapping("/returns/{returnId}/approve")
    public ResponseEntity<ReturnRequestResponseDTO> approveReturn(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email, @PathVariable("returnId") UUID returnId) {
        ReturnRequest approved = adminPort.approveReturn(currentUser.resolve(email), loader.returnRequest(returnId));
        return ResponseEntity.ok(ReturnRequestRestMapper.toResponseDTO(approved,
                loader.refundOfOrder(approved.getOrder().getId())));
    }

    @PatchMapping("/returns/{returnId}/reject")
    public ResponseEntity<ReturnRequestResponseDTO> rejectReturn(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email, @PathVariable("returnId") UUID returnId) {
        ReturnRequest rejected = adminPort.rejectReturn(currentUser.resolve(email), loader.returnRequest(returnId));
        return ResponseEntity.ok(ReturnRequestRestMapper.toResponseDTO(rejected, Optional.empty()));
    }

    @PatchMapping("/refunds/{refundId}/process")
    public ResponseEntity<RefundResponseDTO> processRefund(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email, @PathVariable("refundId") UUID refundId) {
        return ResponseEntity.ok(RefundRestMapper.toResponseDTO(
                adminPort.processRefund(currentUser.resolve(email), loader.refund(refundId))));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLog(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email) {
        return ResponseEntity.ok(adminPort.consultAuditLog(currentUser.resolve(email)).stream()
                .map(AuditLogRestMapper::toResponseDTO).toList());
    }
}