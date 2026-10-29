package application.adapters.rest.controllers;

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
import application.domain.entities.User;
import application.domain.ports.in.SupervisorPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Read-only endpoints of the SUPERVISOR role (OBJ-12). */
@RestController
@RequestMapping("/api/v1/supervisor")
public class SupervisorRestController {

    private final SupervisorPort supervisorPort;

    public SupervisorRestController(SupervisorPort supervisorPort) {
        this.supervisorPort = supervisorPort;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> consultUsers(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(supervisorPort.consultUsers(user).stream()
                .map(UserRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> consultWarehouses(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(supervisorPort.consultWarehouses(user).stream()
                .map(WarehouseRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/inventories")
    public ResponseEntity<List<InventoryResponseDTO>> consultInventory(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(supervisorPort.consultInventory(user).stream()
                .map(InventoryRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> consultOrders(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(supervisorPort.consultOrders(user).stream()
                .map(OrderRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLog(
            @AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(supervisorPort.consultAuditLog(user).stream()
                .map(AuditLogRestMapper::toResponseDTO).toList());
    }

    @GetMapping("/audit-logs/{entityId}")
    public ResponseEntity<List<AuditLogResponseDTO>> consultAuditLogOf(
            @AuthenticationPrincipal(expression = "user") User user, @PathVariable("entityId") UUID entityId) {
        return ResponseEntity.ok(supervisorPort.consultAuditLogOf(user, entityId).stream()
                .map(AuditLogRestMapper::toResponseDTO).toList());
    }
}