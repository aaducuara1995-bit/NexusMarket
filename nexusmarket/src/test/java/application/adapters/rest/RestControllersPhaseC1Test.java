package application.adapters.rest;

import application.adapters.rest.dtos.AddressDTO;
import application.adapters.rest.dtos.requests.OnboardSellerRequestDTO;
import application.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import application.adapters.rest.dtos.requests.RegisterUserRequestDTO;
import application.adapters.rest.dtos.requests.WarehouseRequestDTO;
import application.adapters.rest.dtos.responses.AuditLogResponseDTO;
import application.adapters.rest.dtos.responses.BuyerResponseDTO;
import application.adapters.rest.dtos.responses.ErrorResponseDTO;
import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.adapters.rest.dtos.responses.SellerResponseDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.enums.ProductType;
import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.enums.UserRole;
import application.domain.enums.UserStatus;
import application.domain.enums.WarehouseOwnership;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RestControllersPhaseC1Test {

    private static final String ADMIN = "admin@nexusmarket.com";
    private static final AddressDTO HOME = new AddressDTO("Calle 10 # 20-30", "Medellin", "Antioquia", "050001");

    private static RegisterUserRequestDTO userRequest(String email, String document, UserRole role) {
        return new RegisterUserRequestDTO("Persona", "Prueba", email, document, RestTestContext.PASSWORD, role);
    }

    @Test
    void aBuyerSignsUpPubliclyAndGetsCreated() {
        RestTestContext ctx = new RestTestContext();

        ResponseEntity<BuyerResponseDTO> response = ctx.publicController.registerBuyer(
                new RegisterBuyerRequestDTO("Ana", "Perez", "ana@mail.com", "123456", RestTestContext.PASSWORD, HOME));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(HOME, response.getBody().mainAddress());
        assertEquals(UserRole.BUYER, ctx.users.findById(response.getBody().userId()).orElseThrow().getRole());
        assertTrue(ctx.publicController.consultCatalog().getBody().isEmpty());
    }

    @Test
    void theAdminRegistersAndOnboardsASellerWithItsWarehouse() {
        RestTestContext ctx = new RestTestContext();

        UserResponseDTO seller = ctx.adminController.registerUser(ctx.user(ADMIN),
                userRequest("vende@mail.com", "222", UserRole.SELLER)).getBody();
        ResponseEntity<SellerResponseDTO> onboarded = ctx.adminController.onboardSeller(ctx.user(ADMIN),
                new OnboardSellerRequestDTO(seller.id(),
                        new WarehouseRequestDTO("Bodega Norte", WarehouseType.PHYSICAL, HOME)));

        assertEquals(HttpStatus.CREATED, onboarded.getStatusCode());
        assertEquals(seller.id(), onboarded.getBody().userId());
        assertEquals(1, onboarded.getBody().warehouses().size());
        assertEquals(WarehouseOwnership.SELLER, onboarded.getBody().warehouses().get(0).ownership());
    }

    @Test
    void theAdminBlocksAndActivatesUsers() {
        RestTestContext ctx = new RestTestContext();
        UserResponseDTO supervisor = ctx.adminController.registerUser(ctx.user(ADMIN),
                userRequest("super@mail.com", "333", UserRole.SUPERVISOR)).getBody();

        assertEquals(UserStatus.BLOCKED, ctx.adminController.blockUser(ctx.user(ADMIN), supervisor.id()).getBody().status());
        assertEquals(UserStatus.ACTIVE, ctx.adminController.activateUser(ctx.user(ADMIN), supervisor.id()).getBody().status());
    }

    @Test
    void aDigitalWarehouseNeedsNoAddress() {
        RestTestContext ctx = new RestTestContext();

        var response = ctx.adminController.registerMarketplaceWarehouse(ctx.user(ADMIN),
                new WarehouseRequestDTO("Bodega Digital", WarehouseType.DIGITAL, null));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody().address());
        assertEquals(WarehouseOwnership.MARKETPLACE, response.getBody().ownership());
    }

    @Test
    void approvingAReturnAnswersWithTheRefundToProcess() {
        RestTestContext ctx = new RestTestContext();
        User sellerUser = ctx.adminUseCase.registerUser(ctx.admin, new User(
                new application.domain.valueobjects.FullName("Vende", "Dor"),
                new application.domain.valueobjects.Email("v@mail.com"),
                new application.domain.valueobjects.IdentityDocument("444"), UserRole.SELLER), "secret123");
        ctx.adminController.onboardSeller(ctx.user(ADMIN), new OnboardSellerRequestDTO(sellerUser.getId(),
                new WarehouseRequestDTO("Bodega Digital", WarehouseType.DIGITAL, null)));
        Product ebook = ctx.sellerUseCase.registerProduct(sellerUser, new Product(UUID.randomUUID(), "Ebook",
                null, ProductType.DIGITAL, new Money(new BigDecimal("20000"))));
        ctx.sellerUseCase.publishProduct(sellerUser, ebook);
        BuyerResponseDTO buyer = ctx.publicController.registerBuyer(
                new RegisterBuyerRequestDTO("Ana", "Perez", "ana@mail.com", "555", RestTestContext.PASSWORD, HOME)).getBody();
        User buyerUser = ctx.users.findById(buyer.userId()).orElseThrow();
        ctx.buyerUseCase.addToCart(buyerUser, ebook, 1);
        Order order = ctx.buyerUseCase.confirmOrder(buyerUser, null);
        ctx.buyerUseCase.payOrder(buyerUser, order, order.calculateTotal());
        UUID returnId = ctx.buyerUseCase.requestReturn(buyerUser, order, "No era lo que esperaba").getId();

        ReturnRequestResponseDTO approved = ctx.adminController.approveReturn(ctx.user(ADMIN), returnId).getBody();

        assertEquals(ReturnStatus.APPROVED, approved.status());
        assertNotNull(approved.refundId());
        assertEquals(RefundStatus.PROCESSED,
                ctx.adminController.processRefund(ctx.user(ADMIN), approved.refundId()).getBody().status());
        assertEquals(1, ctx.publicController.consultCatalog().getBody().size());
        assertEquals(0, ctx.publicController.consultAvailability(ebook.getId()).getBody().available());
    }

    @Test
    void theSupervisorReadsEverythingIncludingTheAuditTrail() {
        RestTestContext ctx = new RestTestContext();
        UserResponseDTO supervisor = ctx.adminController.registerUser(ctx.user(ADMIN),
                userRequest("super@mail.com", "333", UserRole.SUPERVISOR)).getBody();
        ctx.adminController.registerMarketplaceWarehouse(ctx.user(ADMIN),
                new WarehouseRequestDTO("Centro", WarehouseType.PHYSICAL, HOME));

        assertEquals(2, ctx.supervisorController.consultUsers(ctx.user("super@mail.com")).getBody().size());
        assertEquals(1, ctx.supervisorController.consultWarehouses(ctx.user("super@mail.com")).getBody().size());
        List<AuditLogResponseDTO> trail = ctx.supervisorController.consultAuditLog(ctx.user("super@mail.com")).getBody();
        assertEquals(AuditOperation.WAREHOUSE_REGISTRATION, trail.get(0).operation());
        assertEquals(1, ctx.supervisorController.consultAuditLogOf(ctx.user("super@mail.com"), supervisor.id()).getBody().size());
    }

    @Test
    void aBuyerCannotUseAdminEndpoints() {
        RestTestContext ctx = new RestTestContext();
        ctx.publicController.registerBuyer(new RegisterBuyerRequestDTO("Ana", "Perez", "ana@mail.com", "123", RestTestContext.PASSWORD, HOME));

        assertThrows(AccessDeniedException.class, () -> ctx.adminController.registerUser(ctx.user("ana@mail.com"),
                userRequest("x@mail.com", "999", UserRole.BUYER)));
        assertThrows(AccessDeniedException.class, () -> ctx.supervisorController.consultOrders(ctx.user("ana@mail.com")));
    }

    @Test
    void anUnknownIdAnswersNotFound() {
        RestTestContext ctx = new RestTestContext();

        assertThrows(EntityNotFoundException.class, () -> ctx.adminController.blockUser(ctx.user(ADMIN), UUID.randomUUID()));
        assertThrows(EntityNotFoundException.class,
                () -> ctx.publicController.consultAvailability(UUID.randomUUID()));
    }

    @Test
    void everyErrorBecomesTheRightHttpStatus() {
        RestTestContext ctx = new RestTestContext();
        var request = RestTestContext.request("PATCH", "/api/v1/admin/users/x/block");

        ResponseEntity<ErrorResponseDTO> unauthenticated =
                ctx.errors.handleInvalidCredentials(new InvalidCredentialsException(), request);
        ResponseEntity<ErrorResponseDTO> forbidden =
                ctx.errors.handleAccessDenied(new AccessDeniedException("no"), request);
        ResponseEntity<ErrorResponseDTO> notFound =
                ctx.errors.handleNotFound(new EntityNotFoundException("User"), request);
        ResponseEntity<ErrorResponseDTO> invalid =
                ctx.errors.handleInvalidData(new IllegalArgumentException("Quantity must be greater than zero."), request);
        ResponseEntity<ErrorResponseDTO> conflict =
                ctx.errors.handleBusinessRule(new IllegalStateException("Not enough stock."), request);

        assertEquals(401, unauthenticated.getBody().status());
        assertEquals(403, forbidden.getBody().status());
        assertEquals(404, notFound.getBody().status());
        assertEquals("User not found.", notFound.getBody().message());
        assertEquals(400, invalid.getBody().status());
        assertEquals(409, conflict.getBody().status());
        assertEquals("/api/v1/admin/users/x/block", conflict.getBody().path());
    }

    @Test
    void repeatedDataIsAConflict() {
        RestTestContext ctx = new RestTestContext();
        ctx.adminController.registerUser(ctx.user(ADMIN), userRequest("dup@mail.com", "777", UserRole.SUPERVISOR));

        IllegalStateException duplicated = assertThrows(IllegalStateException.class,
                () -> ctx.adminController.registerUser(ctx.user(ADMIN), userRequest("dup@mail.com", "778", UserRole.SUPERVISOR)));
        assertEquals(409, ctx.errors.handleBusinessRule(duplicated,
                RestTestContext.request("POST", "/api/v1/admin/users")).getBody().status());
    }
}