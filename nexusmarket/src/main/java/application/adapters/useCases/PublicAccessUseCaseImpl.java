package application.adapters.useCases;

import application.domain.entities.Buyer;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.enums.AuditOperation;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.CheckAvailabilityService;
import application.domain.services.LoginService;
import application.domain.services.RegisterAuditLogService;
import application.domain.services.RegisterBuyerService;
import application.domain.services.RegisterUserService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.AuthenticationResult;
import application.domain.valueobjects.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** Operations anyone can use without signing in: login, buyer sign-up and the public catalog. */
@Service
@Transactional
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final LoginService loginService;
    private final RegisterUserService registerUserService;
    private final RegisterBuyerService registerBuyerService;
    private final CheckAvailabilityService checkAvailabilityService;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final RegisterAuditLogService auditLogService;

    public PublicAccessUseCaseImpl(LoginService loginService,
                                   RegisterUserService registerUserService,
                                   RegisterBuyerService registerBuyerService,
                                   CheckAvailabilityService checkAvailabilityService,
                                   ProductRepositoryPort productRepository,
                                   InventoryRepositoryPort inventoryRepository,
                                   RegisterAuditLogService auditLogService) {
        this.loginService = loginService;
        this.registerUserService = registerUserService;
        this.registerBuyerService = registerBuyerService;
        this.checkAvailabilityService = checkAvailabilityService;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public AuthenticationResult login(Email email, String password) {
        return loginService.login(email, password);
    }

    @Override
    public Buyer registerBuyer(User user, Address mainAddress, String password) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (mainAddress == null)
            throw new IllegalArgumentException("Main address is required.");
        // Without an actor, RegisterUserService only accepts the BUYER role.
        User saved = registerUserService.register(null, user.getFullName(), user.getEmail(),
                user.getDocument(), user.getRole(), password);
        Buyer buyer = registerBuyerService.register(saved, mainAddress);
        auditLogService.register(AuditOperation.USER_REGISTRATION, saved, saved.getId(),
                Map.of("role", saved.getRole().name(), "channel", "public sign-up"));
        return buyer;
    }

    @Override
    public List<Product> consultCatalog() {
        return productRepository.findPublished();
    }

    @Override
    public int consultAvailability(Product product) {
        Product stored = Lookup.reload(product, Product::getId, productRepository::findById, "Product");
        return checkAvailabilityService.availableOf(stored,
                inventoryRepository.findByProduct(stored.getId()));
    }
}