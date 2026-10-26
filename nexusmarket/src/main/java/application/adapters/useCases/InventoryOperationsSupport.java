package application.adapters.useCases;

import application.domain.entities.Inventory;
import application.domain.entities.InventoryMovement;
import application.domain.entities.Product;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.ports.out.InventoryMovementRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.AdjustInventoryService;
import application.domain.services.MarkInventoryService;
import application.domain.services.RegisterInitialInventoryService;
import application.domain.services.RestockInventoryService;

import java.util.List;

/**
 * Inventory operations shared by the seller and the logistic operator. Both roles run
 * the same domain services; the only difference is the seller profile: the seller passes
 * their own so ownership is checked, the operator passes null because they may operate
 * on any product.
 */
public class InventoryOperationsSupport {

    private final RegisterInitialInventoryService registerInitialInventoryService;
    private final AdjustInventoryService adjustInventoryService;
    private final MarkInventoryService markInventoryService;
    private final RestockInventoryService restockInventoryService;
    private final ProductRepositoryPort productRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final ReturnRequestRepositoryPort returnRepository;

    public InventoryOperationsSupport(RegisterInitialInventoryService registerInitialInventoryService,
                                      AdjustInventoryService adjustInventoryService,
                                      MarkInventoryService markInventoryService,
                                      RestockInventoryService restockInventoryService,
                                      ProductRepositoryPort productRepository,
                                      WarehouseRepositoryPort warehouseRepository,
                                      InventoryRepositoryPort inventoryRepository,
                                      InventoryMovementRepositoryPort movementRepository,
                                      ReturnRequestRepositoryPort returnRepository) {
        this.registerInitialInventoryService = registerInitialInventoryService;
        this.adjustInventoryService = adjustInventoryService;
        this.markInventoryService = markInventoryService;
        this.restockInventoryService = restockInventoryService;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.returnRepository = returnRepository;
    }

    public Inventory registerInitialInventory(User user, Seller seller, Product product,
                                              Warehouse warehouse, int quantity) {
        Product storedProduct = Lookup.reload(product, Product::getId, productRepository::findById, "Product");
        Warehouse storedWarehouse = Lookup.reload(warehouse, Warehouse::getId, warehouseRepository::findById, "Warehouse");
        RegisterInitialInventoryService.Result result = registerInitialInventoryService.register(
                user, seller, storedProduct, storedWarehouse, quantity,
                inventoryRepository.findByProduct(storedProduct.getId()));
        inventoryRepository.save(result.inventory());
        movementRepository.saveAll(result.movements());
        return result.inventory();
    }

    public Inventory adjust(User user, Seller seller, Inventory inventory, int difference, String reason) {
        Inventory stored = reload(inventory);
        InventoryMovement movement = adjustInventoryService.adjust(user, seller, stored, difference, reason);
        inventoryRepository.save(stored);
        movementRepository.saveAll(List.of(movement));
        return stored;
    }

    public Inventory markDamaged(User user, Seller seller, Inventory inventory) {
        Inventory stored = reload(inventory);
        markInventoryService.markDamaged(user, seller, stored);
        inventoryRepository.save(stored);
        return stored;
    }

    public Inventory block(User user, Seller seller, Inventory inventory) {
        Inventory stored = reload(inventory);
        markInventoryService.block(user, seller, stored);
        inventoryRepository.save(stored);
        return stored;
    }

    public Inventory enable(User user, Seller seller, Inventory inventory) {
        Inventory stored = reload(inventory);
        markInventoryService.enable(user, seller, stored);
        inventoryRepository.save(stored);
        return stored;
    }

    public Inventory restockFromReturn(User user, Seller seller, Inventory inventory,
                                       ReturnRequest returnRequest, int units) {
        Inventory stored = reload(inventory);
        ReturnRequest storedReturn = Lookup.reload(returnRequest, ReturnRequest::getId,
                returnRepository::findById, "Return request");
        InventoryMovement movement = restockInventoryService.restock(user, seller, stored, units, storedReturn);
        inventoryRepository.save(stored);
        returnRepository.save(storedReturn);
        movementRepository.saveAll(List.of(movement));
        return stored;
    }

    private Inventory reload(Inventory inventory) {
        return Lookup.reload(inventory, Inventory::getId, inventoryRepository::findById, "Inventory");
    }
}