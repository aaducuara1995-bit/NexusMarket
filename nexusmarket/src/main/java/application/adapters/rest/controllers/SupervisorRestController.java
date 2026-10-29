package application.adapters.rest.controllers;

import application.adapters.rest.AuthenticatedUserResolver;
import application.adapters.rest.dtos.responses.AuditLogResponseDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.adapters.rest.dtos.responses.WarehouseResponseDTO;
import application.adapters.rest.mappers.AuditLogRestMapper;
import application.adapters.rest.mappers.InventoryRestMapper;
import application.adapters.rest.mappers.OrderRestMapper;
import application.adapters.rest.mappers.UserRestMapper;
import application.adapters.rest.mappers.WarehouseRestMapper;
import application.domain.ports.in.SupervisorPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Read-only endpoints of the SUPERVISOR role (OBJ-12). */
@RestController
@RequestMapping("/api/v1/supervisor")
public class SupervisorRestController {

    private final SupervisorPort supervisorPort;
    private final AuthenticatedUserResolver currentUser;

    public SupervisorRestController(SupervisorPort supervisorPort, AuthenticatedUserResolver currentUser) {
        this.supervisorPort = supervisorPort;
        this.currentUser = currentUser;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> consultUsers(@RequestHeader(AuthenticatedUserResolver.HEADER) String email) {
        return ResponseEntity.ok(supervisorPort.consultUsers(currentUser.resolve(email)).stream()
                .map(UserRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> consultWarehouses(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email) {
        return ResponseEntity.ok(supervisorPort.consultWarehouses(currentUser.resolve(email)).stream()
                .map(WarehouseRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> consultInventory(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email) {
        return ResponseEntity.ok(supervisorPort.consultInventory(currentUser.resolve(email)).stream()
                .map(InventoryRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> consultOrders(@RequestHeader(AuthenticatedUserResolver.HEADER) String email) {
        return ResponseEntity.ok(supervisorPort.consultOrders(currentUser.resolve(email)).stream()
                .map(OrderRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLog(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email) {
        return ResponseEntity.ok(supervisorPort.consultAuditLog(currentUser.resolve(email)).stream()
                .map(AuditLogRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/audit-logs/{entityId}")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLogOf(
            @RequestHeader(AuthenticatedUserResolver.HEADER) String email, @PathVariable("entityId") UUID entityId) {
        return ResponseEntity.ok(supervisorPort.consultAuditLogOf(currentUser.resolve(email), entityId).stream()
                .map(AuditLogRestMapper::toResponseDTO).toList());
    }
}