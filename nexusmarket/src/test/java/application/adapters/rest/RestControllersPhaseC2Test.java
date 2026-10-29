package application.adapters.rest;

import application.adapters.rest.dtos.AddressDTO;
import application.adapters.rest.dtos.requests.AddToCartRequestDTO;
import application.adapters.rest.dtos.requests.AdjustInventoryRequestDTO;
import application.adapters.rest.dtos.requests.ConfirmOrderRequestDTO;
import application.adapters.rest.dtos.requests.InitialInventoryRequestDTO;
import application.adapters.rest.dtos.requests.OnboardSellerRequestDTO;
import application.adapters.rest.dtos.requests.PaymentRequestDTO;
import application.adapters.rest.dtos.requests.ProductRequestDTO;
import application.adapters.rest.dtos.requests.RegisterBuyerRequestDTO;
import application.adapters.rest.dtos.requests.RegisterUserRequestDTO;
import application.adapters.rest.dtos.requests.RestockRequestDTO;
import application.adapters.rest.dtos.requests.ReturnRequestDTO;
import application.adapters.rest.dtos.requests.WarehouseRequestDTO;
import application.adapters.rest.dtos.responses.CartResponseDTO;
import application.adapters.rest.dtos.responses.InventoryResponseDTO;
import application.adapters.rest.dtos.responses.OrderResponseDTO;
import application.adapters.rest.dtos.responses.ProductResponseDTO;
import application.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import application.adapters.rest.dtos.responses.SellerResponseDTO;
import application.domain.enums.InventoryStatus;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.enums.WarehouseType;
import application.domain.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** The whole business through the REST controllers only, the way Postman would use it. */
class RestControllersPhaseC2Test {

    private static final String ADMIN = "admin@nexusmarket.com";
    private static final String SELLER = "vende@mail.com";
    private static final String BUYER = "compra@mail.com";
    private static final String OPERATOR = "bodega@mail.com";
    private static final AddressDTO HOME = new AddressDTO("Calle 10 # 20-30", "Medellin", "Antioquia", "050001");

    /** Admin creates seller and operator, seller publishes a shirt with 10 units, buyer signs up. */
    private static UUID prepareShop(RestTestContext ctx) {
        UUID sellerUserId = ctx.adminController.registerUser(ADMIN,
                new RegisterUserRequestDTO("Vera", "Vendedora", SELLER, "111", UserRole.SELLER)).getBody().id();
        ctx.adminController.registerUser(ADMIN,
                new RegisterUserRequestDTO("Oscar", "Operador", OPERATOR, "222", UserRole.LOGISTIC_OPERATOR));
        SellerResponseDTO seller = ctx.adminController.onboardSeller(ADMIN, new OnboardSellerRequestDTO(sellerUserId,
                new WarehouseRequestDTO("Bodega Norte", WarehouseType.PHYSICAL, HOME))).getBody();

        ProductResponseDTO shirt = ctx.sellerController.registerProduct(SELLER, new ProductRequestDTO(
                "Camiseta", "Algodon", ProductType.PHYSICAL, new BigDecimal("45000"), null, List.of("S", "M"))).getBody();
        ctx.sellerController.registerInitialInventory(SELLER,
                new InitialInventoryRequestDTO(shirt.id(), seller.warehouses().get(0).id(), 10));
        ctx.sellerController.publishProduct(SELLER, shirt.id());

        ctx.publicController.registerBuyer(new RegisterBuyerRequestDTO("Carla", "Compradora", BUYER, "333", HOME));
        return shirt.id();
    }

    @Test
    void theSellerManagesCatalogAndStock() {
        RestTestContext ctx = new RestTestContext();
        UUID shirtId = prepareShop(ctx);

        ProductResponseDTO listed = ctx.sellerController.consultMyProducts(SELLER).getBody().get(0);
        assertEquals(ProductStatus.PUBLISHED, listed.status());
        assertEquals(List.of("S", "M"), listed.variants());
        assertEquals("COP", listed.currency());

        UUID inventoryId = ctx.inventories.findByProduct(shirtId).get(0).getId();
        InventoryResponseDTO adjusted = ctx.sellerController.adjustInventory(SELLER, inventoryId,
                new AdjustInventoryRequestDTO(-3, "Conteo fisico")).getBody();
        assertEquals(7, adjusted.quantity());
        assertEquals("Bodega Norte", adjusted.warehouseName());
        assertEquals(InventoryStatus.BLOCKED, ctx.sellerController.blockInventory(SELLER, inventoryId).getBody().status());
        assertEquals(0, ctx.publicController.consultAvailability(shirtId).getBody().available());
        assertEquals(InventoryStatus.AVAILABLE, ctx.sellerController.enableInventory(SELLER, inventoryId).getBody().status());
    }

    @Test
    void aPurchaseGoesFromCartToDeliveryAndBackAsAReturn() {
        RestTestContext ctx = new RestTestContext();
        UUID shirtId = prepareShop(ctx);

        CartResponseDTO cart = ctx.buyerController.addToCart(BUYER, new AddToCartRequestDTO(shirtId, 2)).getBody();
        assertEquals(new BigDecimal("90000.00"), cart.total());

        ResponseEntity<OrderResponseDTO> confirmed = ctx.buyerController.confirmOrder(BUYER, null);
        assertEquals(HttpStatus.CREATED, confirmed.getStatusCode());
        OrderResponseDTO order = confirmed.getBody();
        assertEquals(OrderStatus.PENDING_PAYMENT, order.status());
        assertEquals(HOME, order.deliveryAddress());
        assertTrue(ctx.buyerController.consultMyCart(BUYER).getBody().items().isEmpty());

        OrderResponseDTO paid = ctx.buyerController.payOrder(BUYER, order.id(),
                new PaymentRequestDTO(order.total(), null)).getBody();
        assertEquals(OrderStatus.PAID, paid.status());

        assertEquals(1, ctx.logisticController.consultOrdersToShip(OPERATOR).getBody().size());
        assertEquals(OrderStatus.SHIPPED, ctx.logisticController.shipOrder(OPERATOR, order.id()).getBody().status());
        assertEquals(OrderStatus.DELIVERED, ctx.logisticController.confirmDelivery(OPERATOR, order.id()).getBody().status());
        assertEquals(8, ctx.publicController.consultAvailability(shirtId).getBody().available());

        ResponseEntity<ReturnRequestResponseDTO> requested = ctx.buyerController.requestReturn(BUYER, order.id(),
                new ReturnRequestDTO("Talla equivocada"));
        assertEquals(HttpStatus.CREATED, requested.getStatusCode());
        ReturnRequestResponseDTO approved = ctx.adminController.approveReturn(ADMIN, requested.getBody().id()).getBody();
        ctx.adminController.processRefund(ADMIN, approved.refundId());

        UUID inventoryId = ctx.inventories.findByProduct(shirtId).get(0).getId();
        InventoryResponseDTO restocked = ctx.logisticController.restockFromReturn(OPERATOR, inventoryId,
                new RestockRequestDTO(approved.id(), 2)).getBody();
        assertEquals(10, restocked.quantity());
        assertEquals(1, ctx.buyerController.consultMyOrders(BUYER).getBody().size());
    }

    @Test
    void theBuyerCanChooseOneOfTheirAddressesAndRemoveLines() {
        RestTestContext ctx = new RestTestContext();
        UUID shirtId = prepareShop(ctx);

        ctx.buyerController.addToCart(BUYER, new AddToCartRequestDTO(shirtId, 1));
        assertTrue(ctx.buyerController.removeFromCart(BUYER, shirtId).getBody().items().isEmpty());

        ctx.buyerController.addToCart(BUYER, new AddToCartRequestDTO(shirtId, 1));
        AddressDTO other = new AddressDTO("Otra casa", "Bello", "Antioquia", "051050");
        assertThrows(IllegalArgumentException.class,
                () -> ctx.buyerController.confirmOrder(BUYER, new ConfirmOrderRequestDTO(other)));
        assertEquals(HOME, ctx.buyerController.confirmOrder(BUYER, new ConfirmOrderRequestDTO(HOME))
                .getBody().deliveryAddress());
    }

    @Test
    void aWrongPaymentAndMissingStockAreRejected() {
        RestTestContext ctx = new RestTestContext();
        UUID shirtId = prepareShop(ctx);

        assertThrows(IllegalStateException.class,
                () -> ctx.buyerController.addToCart(BUYER, new AddToCartRequestDTO(shirtId, 50)));
        ctx.buyerController.addToCart(BUYER, new AddToCartRequestDTO(shirtId, 1));
        UUID orderId = ctx.buyerController.confirmOrder(BUYER, null).getBody().id();
        assertThrows(IllegalArgumentException.class, () -> ctx.buyerController.payOrder(BUYER, orderId,
                new PaymentRequestDTO(new BigDecimal("1000"), null)));
    }

    @Test
    void eachRoleStaysInItsOwnEndpoints() {
        RestTestContext ctx = new RestTestContext();
        UUID shirtId = prepareShop(ctx);

        assertThrows(AccessDeniedException.class, () -> ctx.sellerController.consultMyProducts(BUYER));
        assertThrows(AccessDeniedException.class, () -> ctx.buyerController.consultMyCart(SELLER));
        assertThrows(AccessDeniedException.class, () -> ctx.logisticController.consultOrdersToShip(SELLER));
        assertThrows(AccessDeniedException.class, () -> ctx.sellerController.publishProduct(OPERATOR, shirtId));
    }
}