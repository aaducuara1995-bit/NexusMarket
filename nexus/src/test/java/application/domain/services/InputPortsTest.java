package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Cart;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.OrderStatus;
import application.domain.enums.ProductType;
import application.domain.enums.UserRole;
import application.domain.ports.in.*;
import application.domain.valueobjects.Money;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputPortsTest {

    @Test
    void eachInputPortHasItsService() {
        Map<Class<?>, Class<?>> pairs = Map.ofEntries(
                Map.entry(OnboardSellerUseCase.class, OnboardSellerService.class),
                Map.entry(PublishProductUseCase.class, PublishProductService.class),
                Map.entry(ChangeProductStatusUseCase.class, ChangeProductStatusService.class),
                Map.entry(CheckAvailabilityUseCase.class, CheckAvailabilityService.class),
                Map.entry(RegisterInitialInventoryUseCase.class, RegisterInitialInventoryService.class),
                Map.entry(AdjustInventoryUseCase.class, AdjustInventoryService.class),
                Map.entry(MarkInventoryUseCase.class, MarkInventoryService.class),
                Map.entry(RestockInventoryUseCase.class, RestockInventoryService.class),
                Map.entry(AddToCartUseCase.class, AddToCartService.class),
                Map.entry(ConfirmOrderUseCase.class, ConfirmOrderService.class),
                Map.entry(PayOrderUseCase.class, PayOrderService.class),
                Map.entry(DeliverDigitalOrderUseCase.class, DeliverDigitalOrderService.class),
                Map.entry(IssueInvoiceUseCase.class, IssueInvoiceService.class),
                Map.entry(CreateShipmentUseCase.class, CreateShipmentService.class),
                Map.entry(ShipOrderUseCase.class, ShipOrderService.class),
                Map.entry(ConfirmDeliveryUseCase.class, ConfirmDeliveryService.class),
                Map.entry(RequestReturnUseCase.class, RequestReturnService.class),
                Map.entry(ResolveReturnUseCase.class, ResolveReturnService.class),
                Map.entry(GenerateRefundUseCase.class, GenerateRefundService.class),
                Map.entry(ProcessRefundUseCase.class, ProcessRefundService.class),
                Map.entry(RegisterUserUseCase.class, RegisterUserService.class),
                Map.entry(RegisterBuyerUseCase.class, RegisterBuyerService.class),
                Map.entry(RegisterSellerUseCase.class, RegisterSellerService.class),
                Map.entry(RegisterWarehouseUseCase.class, RegisterWarehouseService.class),
                Map.entry(RegisterProductUseCase.class, RegisterProductService.class));

        assertEquals(25, pairs.size());
        pairs.forEach((port, service) ->
                assertTrue(port.isAssignableFrom(service)));
    }

    @Test
    void aFullPurchaseRunsOnlyThroughThePorts() {
        User user = Fixtures.user(UserRole.BUYER);
        Buyer buyer = Fixtures.buyer(user);
        Product digital = Fixtures.product(ProductType.DIGITAL);
        digital.publish();
        Cart cart = new Cart(buyer);

        BuyerValidationService validation = new BuyerValidationService();
        AddToCartUseCase add = new AddToCartService(validation);
        ConfirmOrderUseCase confirm = new ConfirmOrderService(
                validation, new ReserveInventoryService(), new ReleaseReservationService());
        PayOrderUseCase pay = new PayOrderService(validation);
        DeliverDigitalOrderUseCase deliver = new DeliverDigitalOrderService();

        add.add(user, buyer, cart, digital, 2, List.of());
        Order order = confirm.confirm(user, buyer, cart, List.of()).order();
        Money total = order.calculateTotal();
        pay.pay(user, buyer, order, total);
        deliver.deliver(order);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }
}