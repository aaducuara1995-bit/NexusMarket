package application.adapters.useCases;

import application.domain.entities.Inventory;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.ports.in.SellerPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.services.AuthorizationService;
import application.domain.services.ChangeProductStatusService;
import application.domain.services.PublishProductService;
import application.domain.services.RegisterProductService;

import java.util.List;

/** Operations of the SELLER role: their catalog and the inventory of their products. */
public class SellerUseCaseImpl implements SellerPort {

    private final AuthorizationService authorizationService;
    private final RegisterProductService registerProductService;
    private final PublishProductService publishProductService;
    private final ChangeProductStatusService changeProductStatusService;
    private final InventoryOperationsSupport inventoryOperations;
    private final SellerRepositoryPort sellerRepository;
    private final ProductRepositoryPort productRepository;
    private final InventoryRepositoryPort inventoryRepository;

    public SellerUseCaseImpl(AuthorizationService authorizationService,
                             RegisterProductService registerProductService,
                             PublishProductService publishProductService,
                             ChangeProductStatusService changeProductStatusService,
                             InventoryOperationsSupport inventoryOperations,
                             SellerRepositoryPort sellerRepository,
                             ProductRepositoryPort productRepository,
                             InventoryRepositoryPort inventoryRepository) {
        this.authorizationService = authorizationService;
        this.registerProductService = registerProductService;
        this.publishProductService = publishProductService;
        this.changeProductStatusService = changeProductStatusService;
        this.inventoryOperations = inventoryOperations;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /** The product that arrives is a draft: the stored one always belongs to the signed-in seller. */
    @Override
    public Product registerProduct(User user, Product product) {
        Seller seller = sellerOf(user);
        if (product == null)
            throw new IllegalArgumentException("Product is required.");
        Product registered = registerProductService.register(user, seller, product.getName(),
                product.getDescription(), product.getType(), product.getPrice());
        if (!product.getVariants().isEmpty()) {
            product.getVariants().forEach(registered::addVariant);
            productRepository.save(registered);
        }
        return registered;
    }

    @Override
    public Product publishProduct(User user, Product product) {
        Seller seller = sellerOf(user);
        Product stored = reload(product);
        publishProductService.publish(user, seller, stored, inventoryRepository.findByProduct(stored.getId()));
        productRepository.save(stored);
        return stored;
    }

    @Override
    public Product suspendProduct(User user, Product product) {
        Seller seller = sellerOf(user);
        Product stored = reload(product);
        changeProductStatusService.suspend(user, seller, stored);
        productRepository.save(stored);
        return stored;
    }

    @Override
    public Product discontinueProduct(User user, Product product) {
        Seller seller = sellerOf(user);
        Product stored = reload(product);
        changeProductStatusService.discontinue(user, seller, stored,
                inventoryRepository.findByProduct(stored.getId()));
        productRepository.save(stored);
        return stored;
    }

    @Override
    public List<Product> consultMyProducts(User user) {
        authorizationService.requirePermission(user, BusinessProcess.PRODUCT_REGISTRATION);
        return productRepository.findBySeller(sellerOf(user).getId());
    }

    @Override
    public Inventory registerInitialInventory(User user, Product product, Warehouse warehouse, int quantity) {
        return inventoryOperations.registerInitialInventory(user, sellerOf(user), product, warehouse, quantity);
    }

    @Override
    public Inventory adjustInventory(User user, Inventory inventory, int difference, String reason) {
        return inventoryOperations.adjust(user, sellerOf(user), inventory, difference, reason);
    }

    @Override
    public Inventory markInventoryDamaged(User user, Inventory inventory) {
        return inventoryOperations.markDamaged(user, sellerOf(user), inventory);
    }

    @Override
    public Inventory blockInventory(User user, Inventory inventory) {
        return inventoryOperations.block(user, sellerOf(user), inventory);
    }

    @Override
    public Inventory enableInventory(User user, Inventory inventory) {
        return inventoryOperations.enable(user, sellerOf(user), inventory);
    }

    @Override
    public Inventory restockFromReturn(User user, Inventory inventory, ReturnRequest returnRequest, int units) {
        return inventoryOperations.restockFromReturn(user, sellerOf(user), inventory, returnRequest, units);
    }

    private Seller sellerOf(User user) {
        if (user == null)
            throw new IllegalArgumentException("User is required.");
        if (!user.hasRole(UserRole.SELLER))
            throw new AccessDeniedException("The user does not have the seller role.");
        return sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Seller profile"));
    }

    private Product reload(Product product) {
        return Lookup.reload(product, Product::getId, productRepository::findById, "Product");
    }
}