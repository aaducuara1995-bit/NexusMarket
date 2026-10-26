package application.adapters.useCases;

import application.domain.entities.Buyer;
import application.domain.entities.Product;
import application.domain.entities.User;
import application.domain.ports.in.PublicAccessPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.services.CheckAvailabilityService;
import application.domain.services.RegisterBuyerService;
import application.domain.services.RegisterUserService;
import application.domain.valueobjects.Address;

import java.util.List;

/** Operations anyone can use without signing in: buyer sign-up and the public catalog. */
public class PublicAccessUseCaseImpl implements PublicAccessPort {

    private final RegisterUserService registerUserService;
    private final RegisterBuyerService registerBuyerService;
    private final CheckAvailabilityService checkAvailabilityService;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;

    public PublicAccessUseCaseImpl(RegisterUserService registerUserService,
                                   RegisterBuyerService registerBuyerService,
                                   CheckAvailabilityService checkAvailabilityService,
                                   ProductRepositoryPort productRepository,
                                   InventoryRepositoryPort inventoryRepository) {
        this.registerUserService = registerUserService;
        this.registerBuyerService = registerBuyerService;
        this.checkAvailabilityService = checkAvailabilityService;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public Buyer registerBuyer(User user, Address mainAddress) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (mainAddress == null)
            throw new IllegalArgumentException("Main address is required.");
        // Without an actor, RegisterUserService only accepts the BUYER role.
        User saved = registerUserService.register(null, user.getFullName(), user.getEmail(),
                user.getDocument(), user.getRole());
        return registerBuyerService.register(saved, mainAddress);
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