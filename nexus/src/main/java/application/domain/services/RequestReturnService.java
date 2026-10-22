package application.domain.services;

import application.domain.entities.Buyer;
import application.domain.entities.Order;
import application.domain.entities.ReturnRequest;
import application.domain.entities.User;
import application.domain.enums.BusinessProcess;
import application.domain.enums.UserRole;
import application.domain.exceptions.AccessDeniedException;
import application.domain.ports.in.RequestReturnUseCase;

import java.util.List;

public class RequestReturnService implements RequestReturnUseCase {

    private final AuthorizationService authorization;

    public RequestReturnService(AuthorizationService authorization) {
        if (authorization == null)
            throw new IllegalArgumentException("Authorization service is required.");
        this.authorization = authorization;
    }

    public ReturnRequest request(User user, Buyer buyer, Order order,
                                String reason, List<ReturnRequest> existing) {
        authorization.requirePermission(user, BusinessProcess.RETURN_MANAGEMENT);
        if (buyer == null)
            throw new IllegalArgumentException("Buyer is required.");
        if (order == null)
            throw new IllegalArgumentException("Order is required.");
        if (existing == null)
            throw new IllegalArgumentException("Existing return list is required.");

        if (!user.hasRole(UserRole.BUYER))
            throw new AccessDeniedException("Only the buyer can request a return.");
        if (!buyer.getUserId().equals(user.getId()))
            throw new AccessDeniedException("The buyer does not match this user.");
        if (!order.belongsTo(buyer.getId()))
            throw new AccessDeniedException("The order belongs to another buyer.");

        boolean anotherOpen = existing.stream()
                .anyMatch(d -> d.getOrder().getId().equals(order.getId()) && d.isOpen());
        if (anotherOpen)
            throw new IllegalStateException("The order already has a return in progress or approved.");

        // The return itself requires a delivered order and a reason.
        return new ReturnRequest(order, reason);
    }
}