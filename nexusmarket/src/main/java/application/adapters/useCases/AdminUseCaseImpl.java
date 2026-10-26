package application.adapters.useCases;

import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;
import application.domain.enums.BusinessProcess;
import application.domain.ports.in.AdminPort;
import application.domain.ports.out.RefundRepositoryPort;
import application.domain.ports.out.ReturnRequestRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.services.AuthorizationService;
import application.domain.services.GenerateRefundService;
import application.domain.services.OnboardSellerService;
import application.domain.services.ProcessRefundService;
import application.domain.services.RegisterUserService;
import application.domain.services.RegisterWarehouseService;
import application.domain.services.ResolveReturnService;

import java.util.List;

/** Operations of the ADMIN role: users, sellers, warehouses and after-sales decisions. */
public class AdminUseCaseImpl implements AdminPort {

    private final AuthorizationService authorizationService;
    private final RegisterUserService registerUserService;
    private final OnboardSellerService onboardSellerService;
    private final RegisterWarehouseService registerWarehouseService;
    private final ResolveReturnService resolveReturnService;
    private final GenerateRefundService generateRefundService;
    private final ProcessRefundService processRefundService;
    private final UserRepositoryPort userRepository;
    private final SellerRepositoryPort sellerRepository;
    private final WarehouseRepositoryPort warehouseRepository;
    private final ReturnRequestRepositoryPort returnRepository;
    private final RefundRepositoryPort refundRepository;

    public AdminUseCaseImpl(AuthorizationService authorizationService,
                            RegisterUserService registerUserService,
                            OnboardSellerService onboardSellerService,
                            RegisterWarehouseService registerWarehouseService,
                            ResolveReturnService resolveReturnService,
                            GenerateRefundService generateRefundService,
                            ProcessRefundService processRefundService,
                            UserRepositoryPort userRepository,
                            SellerRepositoryPort sellerRepository,
                            WarehouseRepositoryPort warehouseRepository,
                            ReturnRequestRepositoryPort returnRepository,
                            RefundRepositoryPort refundRepository) {
        this.authorizationService = authorizationService;
        this.registerUserService = registerUserService;
        this.onboardSellerService = onboardSellerService;
        this.registerWarehouseService = registerWarehouseService;
        this.resolveReturnService = resolveReturnService;
        this.generateRefundService = generateRefundService;
        this.processRefundService = processRefundService;
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.warehouseRepository = warehouseRepository;
        this.returnRepository = returnRepository;
        this.refundRepository = refundRepository;
    }

    @Override
    public User registerUser(User admin, User newUser) {
        authorizationService.requirePermission(admin, BusinessProcess.USER_MANAGEMENT);
        if (newUser == null)
            throw new IllegalArgumentException("The user to register is required.");
        return registerUserService.register(admin, newUser.getFullName(), newUser.getEmail(),
                newUser.getDocument(), newUser.getRole());
    }

    @Override
    public User blockUser(User admin, User target) {
        User stored = targetOf(admin, target);
        if (stored.getId().equals(admin.getId()))
            throw new IllegalStateException("An admin cannot block their own user.");
        stored.block();
        userRepository.save(stored);
        return stored;
    }

    @Override
    public User activateUser(User admin, User target) {
        User stored = targetOf(admin, target);
        stored.activate();
        userRepository.save(stored);
        return stored;
    }

    /** Creates the seller profile and its first warehouse in one step (RN-01). */
    @Override
    public Seller onboardSeller(User admin, User sellerUser, Warehouse firstWarehouse) {
        authorizationService.requirePermission(admin, BusinessProcess.SELLER_REGISTRATION);
        User storedUser = Lookup.reload(sellerUser, User::getId, userRepository::findById, "User");
        if (firstWarehouse == null)
            throw new IllegalArgumentException("The first warehouse is required.");
        if (sellerRepository.existsByUserId(storedUser.getId()))
            throw new IllegalStateException("The user already has a seller profile.");
        if (warehouseRepository.existsByName(firstWarehouse.getName()))
            throw new IllegalStateException("A warehouse with that name already exists.");

        OnboardSellerService.Result result = onboardSellerService.onboard(admin, storedUser,
                firstWarehouse.getName(), firstWarehouse.getType(), firstWarehouse.getAddress());

        sellerRepository.save(result.seller());
        warehouseRepository.save(result.warehouse());
        return result.seller();
    }

    @Override
    public Warehouse registerMarketplaceWarehouse(User admin, Warehouse warehouse) {
        if (warehouse == null)
            throw new IllegalArgumentException("Warehouse is required.");
        return registerWarehouseService.registerForMarketplace(admin, warehouse.getName(),
                warehouse.getType(), warehouse.getAddress());
    }

    @Override
    public Warehouse registerSellerWarehouse(User admin, Seller seller, Warehouse warehouse) {
        authorizationService.requirePermission(admin, BusinessProcess.WAREHOUSE_MANAGEMENT);
        Seller storedSeller = Lookup.reload(seller, Seller::getId, sellerRepository::findById, "Seller");
        if (warehouse == null)
            throw new IllegalArgumentException("Warehouse is required.");
        return registerWarehouseService.registerForSeller(admin, storedSeller, warehouse.getName(),
                warehouse.getType(), warehouse.getAddress());
    }

    /** Approving a return also generates its refund, which stays pending until processed. */
    @Override
    public ReturnRequest approveReturn(User admin, ReturnRequest returnRequest) {
        ReturnRequest stored = reloadReturn(returnRequest);
        resolveReturnService.approve(admin, stored);
        Refund refund = generateRefundService.generate(stored,
                refundRepository.findByOrder(stored.getOrder().getId()).map(List::of).orElse(List.of()));
        returnRepository.save(stored);
        refundRepository.save(refund);
        return stored;
    }

    @Override
    public ReturnRequest rejectReturn(User admin, ReturnRequest returnRequest) {
        ReturnRequest stored = reloadReturn(returnRequest);
        resolveReturnService.reject(admin, stored);
        returnRepository.save(stored);
        return stored;
    }

    @Override
    public Refund processRefund(User admin, Refund refund) {
        Refund stored = Lookup.reload(refund, Refund::getId, refundRepository::findById, "Refund");
        processRefundService.process(admin, stored);
        refundRepository.save(stored);
        return stored;
    }

    private User targetOf(User admin, User target) {
        authorizationService.requirePermission(admin, BusinessProcess.USER_MANAGEMENT);
        return Lookup.reload(target, User::getId, userRepository::findById, "User");
    }

    private ReturnRequest reloadReturn(ReturnRequest returnRequest) {
        return Lookup.reload(returnRequest, ReturnRequest::getId, returnRepository::findById, "Return request");
    }
}