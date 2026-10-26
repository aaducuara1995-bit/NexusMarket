package application.adapters.config;

import application.domain.ports.out.AuditLogRepositoryPort;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.AddToCartService;
import application.domain.services.AdjustInventoryService;
import application.domain.services.AuthorizationService;
import application.domain.services.BuyerValidationService;
import application.domain.services.ChangeProductStatusService;
import application.domain.services.CheckAvailabilityService;
import application.domain.services.ConfirmDeliveryService;
import application.domain.services.ConfirmOrderService;
import application.domain.services.ConsultAuditLogService;
import application.domain.services.CreateShipmentService;
import application.domain.services.DeliverDigitalOrderService;
import application.domain.services.GenerateRefundService;
import application.domain.services.InventoryAccessService;
import application.domain.services.InventoryOutflowService;
import application.domain.services.IssueInvoiceService;
import application.domain.services.MarkInventoryService;
import application.domain.services.OnboardSellerService;
import application.domain.services.PayOrderService;
import application.domain.services.ProcessRefundService;
import application.domain.services.PublishProductService;
import application.domain.services.RegisterAuditLogService;
import application.domain.services.RegisterBuyerService;
import application.domain.services.RegisterInitialInventoryService;
import application.domain.services.RegisterProductService;
import application.domain.services.RegisterSellerService;
import application.domain.services.RegisterUserService;
import application.domain.services.RegisterWarehouseService;
import application.domain.services.ReleaseReservationService;
import application.domain.services.RequestReturnService;
import application.domain.services.ReserveInventoryService;
import application.domain.services.ResolveReturnService;
import application.domain.services.RestockInventoryService;
import application.domain.services.SellerValidationService;
import application.domain.services.ShipOrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the domain services as Spring beans. The domain stays free of Spring
 * annotations: this class, which belongs to the adapters layer, is the only place that
 * knows how to build them. Spring injects the output ports with the persistence adapters
 * (JPA for MySQL, MongoDB for the audit log).
 */
@Configuration
public class DomainServicesConfig {

    @Bean
    public AddToCartService addToCartService(BuyerValidationService buyerValidationService) {
        return new AddToCartService(buyerValidationService);
    }

    @Bean
    public AdjustInventoryService adjustInventoryService(InventoryAccessService inventoryAccessService) {
        return new AdjustInventoryService(inventoryAccessService);
    }

    @Bean
    public AuthorizationService authorizationService() {
        return new AuthorizationService();
    }

    @Bean
    public BuyerValidationService buyerValidationService() {
        return new BuyerValidationService();
    }

    @Bean
    public ChangeProductStatusService changeProductStatusService(AuthorizationService authorizationService, SellerValidationService sellerValidationService) {
        return new ChangeProductStatusService(authorizationService, sellerValidationService);
    }

    @Bean
    public CheckAvailabilityService checkAvailabilityService() {
        return new CheckAvailabilityService();
    }

    @Bean
    public ConfirmDeliveryService confirmDeliveryService(AuthorizationService authorizationService) {
        return new ConfirmDeliveryService(authorizationService);
    }

    @Bean
    public ConfirmOrderService confirmOrderService(BuyerValidationService buyerValidationService, ReserveInventoryService reserveInventoryService, ReleaseReservationService releaseReservationService) {
        return new ConfirmOrderService(buyerValidationService, reserveInventoryService, releaseReservationService);
    }

    @Bean
    public ConsultAuditLogService consultAuditLogService(AuthorizationService authorizationService, AuditLogRepositoryPort auditLogRepositoryPort) {
        return new ConsultAuditLogService(authorizationService, auditLogRepositoryPort);
    }

    @Bean
    public CreateShipmentService createShipmentService() {
        return new CreateShipmentService();
    }

    @Bean
    public DeliverDigitalOrderService deliverDigitalOrderService() {
        return new DeliverDigitalOrderService();
    }

    @Bean
    public GenerateRefundService generateRefundService() {
        return new GenerateRefundService();
    }

    @Bean
    public InventoryAccessService inventoryAccessService(AuthorizationService authorizationService, SellerValidationService sellerValidationService) {
        return new InventoryAccessService(authorizationService, sellerValidationService);
    }

    @Bean
    public InventoryOutflowService inventoryOutflowService() {
        return new InventoryOutflowService();
    }

    @Bean
    public IssueInvoiceService issueInvoiceService() {
        return new IssueInvoiceService();
    }

    @Bean
    public MarkInventoryService markInventoryService(InventoryAccessService inventoryAccessService) {
        return new MarkInventoryService(inventoryAccessService);
    }

    @Bean
    public OnboardSellerService onboardSellerService(AuthorizationService authorizationService) {
        return new OnboardSellerService(authorizationService);
    }

    @Bean
    public PayOrderService payOrderService(BuyerValidationService buyerValidationService) {
        return new PayOrderService(buyerValidationService);
    }

    @Bean
    public ProcessRefundService processRefundService(AuthorizationService authorizationService) {
        return new ProcessRefundService(authorizationService);
    }

    @Bean
    public PublishProductService publishProductService(AuthorizationService authorizationService, SellerValidationService sellerValidationService) {
        return new PublishProductService(authorizationService, sellerValidationService);
    }

    @Bean
    public RegisterAuditLogService registerAuditLogService(AuditLogRepositoryPort auditLogRepositoryPort) {
        return new RegisterAuditLogService(auditLogRepositoryPort);
    }

    @Bean
    public RegisterBuyerService registerBuyerService(BuyerRepositoryPort buyerRepositoryPort) {
        return new RegisterBuyerService(buyerRepositoryPort);
    }

    @Bean
    public RegisterInitialInventoryService registerInitialInventoryService(InventoryAccessService inventoryAccessService) {
        return new RegisterInitialInventoryService(inventoryAccessService);
    }

    @Bean
    public RegisterProductService registerProductService(AuthorizationService authorizationService, SellerValidationService sellerValidationService, ProductRepositoryPort productRepositoryPort) {
        return new RegisterProductService(authorizationService, sellerValidationService, productRepositoryPort);
    }

    @Bean
    public RegisterSellerService registerSellerService(AuthorizationService authorizationService, SellerRepositoryPort sellerRepositoryPort) {
        return new RegisterSellerService(authorizationService, sellerRepositoryPort);
    }

    @Bean
    public RegisterUserService registerUserService(AuthorizationService authorizationService, UserRepositoryPort userRepositoryPort) {
        return new RegisterUserService(authorizationService, userRepositoryPort);
    }

    @Bean
    public RegisterWarehouseService registerWarehouseService(AuthorizationService authorizationService, WarehouseRepositoryPort warehouseRepositoryPort) {
        return new RegisterWarehouseService(authorizationService, warehouseRepositoryPort);
    }

    @Bean
    public ReleaseReservationService releaseReservationService() {
        return new ReleaseReservationService();
    }

    @Bean
    public RequestReturnService requestReturnService(AuthorizationService authorizationService) {
        return new RequestReturnService(authorizationService);
    }

    @Bean
    public ReserveInventoryService reserveInventoryService() {
        return new ReserveInventoryService();
    }

    @Bean
    public ResolveReturnService resolveReturnService(AuthorizationService authorizationService) {
        return new ResolveReturnService(authorizationService);
    }

    @Bean
    public RestockInventoryService restockInventoryService(InventoryAccessService inventoryAccessService) {
        return new RestockInventoryService(inventoryAccessService);
    }

    @Bean
    public SellerValidationService sellerValidationService() {
        return new SellerValidationService();
    }

    @Bean
    public ShipOrderService shipOrderService(AuthorizationService authorizationService, InventoryOutflowService inventoryOutflowService) {
        return new ShipOrderService(authorizationService, inventoryOutflowService);
    }
}