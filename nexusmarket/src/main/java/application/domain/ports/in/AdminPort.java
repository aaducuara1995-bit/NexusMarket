package application.domain.ports.in;

import application.domain.entities.AuditLog;
import application.domain.entities.Refund;
import application.domain.entities.ReturnRequest;
import application.domain.entities.Seller;
import application.domain.entities.User;
import application.domain.entities.Warehouse;

import java.util.List;

/** Operations of the ADMIN role: users, sellers, warehouses and after-sales decisions. */
public interface AdminPort {

    User registerUser(User admin, User newUser);

    User blockUser(User admin, User target);

    User activateUser(User admin, User target);

    Seller onboardSeller(User admin, User sellerUser, Warehouse firstWarehouse);

    Warehouse registerMarketplaceWarehouse(User admin, Warehouse warehouse);

    Warehouse registerSellerWarehouse(User admin, Seller seller, Warehouse warehouse);

    ReturnRequest approveReturn(User admin, ReturnRequest returnRequest);

    ReturnRequest rejectReturn(User admin, ReturnRequest returnRequest);

    Refund processRefund(User admin, Refund refund);

    List<AuditLog> consultAuditLog(User admin);
}